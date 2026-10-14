package com.naveenapps.expensemanager.feature.agent

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naveenapps.expensemanager.core.data.repository.FullDataBackup
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class FullDataBackupIntegrationTest {
    private lateinit var root: File
    private lateinit var context: Context
    private lateinit var db: ExpenseManagerDatabase
    @Before fun setUp() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        root = File(base.cacheDir, "backup-test-${UUID.randomUUID()}").apply { mkdirs() }
        context = object : ContextWrapper(base) {
            override fun getFilesDir() = File(root, "files").apply { mkdirs() }
            override fun getCacheDir() = File(root, "cache").apply { mkdirs() }
            override fun getNoBackupFilesDir() = File(root, "no_backup").apply { mkdirs() }
            override fun getDatabasePath(name: String) = if (File(name).isAbsolute) File(name) else File(File(root, "databases").apply { mkdirs() }, name)
            override fun getApplicationInfo() = ApplicationInfo(base.applicationInfo).apply { dataDir = root.path }
        }
        db = open()
        val sql = db.openHelper.writableDatabase
        sql.execSQL("INSERT INTO account (id,name,type,icon_background_color,icon_name,amount,credit_limit,sequence,created_on,updated_on,initial_balance_cents,current_balance_cents,ledger_id) VALUES ('a','微信',0,'#000','wallet',123.45,0,0,1,1,12345,12345,'default')")
        sql.execSQL("INSERT INTO anxiety_relief_item (id,name,image_uri,price_cents,purchase_date,target_daily_cost_cents,note,created_at,updated_at,is_archived) VALUES ('comfort','冰箱',NULL,26900,'2026-08-13',100,'宿舍',1,1,0)")
        sql.execSQL("INSERT INTO monthly_saving (id,year,month,amount_cents,note,created_at,updated_at) VALUES (1,2026,10,88000,'储蓄',1,1)")
        val attachment = File(context.filesDir, "appearance/background").apply { parentFile!!.mkdirs(); writeBytes(byteArrayOf(1,2,3,4)) }
        File(context.filesDir, "plans.json").writeText("{\"savingsPlan\":\"新电脑\",\"targetCents\":500000}")
        val preferences = androidx.datastore.preferences.PreferencesProto.PreferenceMap.newBuilder()
            .putPreferences("jiangji_savings_plans_json", androidx.datastore.preferences.PreferencesProto.Value.newBuilder().setString("[{\"id\":\"plan\",\"targetCents\":500000,\"savedCents\":12345}]").build()).build()
        File(context.filesDir,"datastore/expense_manager_app_data_store.preferences_pb").apply { parentFile!!.mkdirs(); outputStream().use { preferences.writeTo(it) } }
        File(File(root,"shared_prefs").apply { mkdirs() },"backup-test.xml").writeText("<?xml version='1.0' encoding='utf-8' standalone='yes' ?><map><string name=\"background\">${attachment.path}</string><boolean name=\"enabled\" value=\"true\" /></map>")
    }
    private fun open() = Room.databaseBuilder(context, ExpenseManagerDatabase::class.java, context.getDatabasePath("expense_manager_database.db").path).build()
    @After fun clean() { if (db.isOpen) db.close(); root.deleteRecursively() }
    private fun archive(): ByteArray = ByteArrayOutputStream().also { FullDataBackup.export(context, db, it) }.toByteArray()

    @Test fun snapshotIncludesAllTablesSettingsAndFilesAndRestoresWithoutDuplicating() {
        val source = archive()
        assertEquals(0, FullDataBackup.stage(context, ByteArrayInputStream(source)))
        assertEquals(12345L, db.openHelper.writableDatabase.query("SELECT current_balance_cents FROM account WHERE id='a'").use { it.moveToFirst(); it.getLong(0) })
        db.openHelper.writableDatabase.execSQL("UPDATE account SET current_balance_cents=99999 WHERE id='a'")
        File(context.filesDir,"plans.json").writeText("changed")
        db.close()
        FullDataBackup.applyBeforeStartup(context)
        val failure = File(context.noBackupFilesDir,"full-restore/restore-failed.txt")
        assertFalse("Restore error: ${if (failure.exists()) failure.readText() else "none"}",failure.exists())
        db = open()
        assertEquals(12345L, db.openHelper.writableDatabase.query("SELECT current_balance_cents FROM account WHERE id='a'").use { it.moveToFirst(); it.getLong(0) })
        assertTrue(File(context.filesDir,"plans.json").readText().contains("新电脑"))
        assertEquals(26900L, db.openHelper.writableDatabase.query("SELECT price_cents FROM anxiety_relief_item WHERE id='comfort'").use { it.moveToFirst(); it.getLong(0) })
        assertEquals(88000L, db.openHelper.writableDatabase.query("SELECT amount_cents FROM monthly_saving WHERE id=1").use { it.moveToFirst(); it.getLong(0) })
        val preferences = File(context.filesDir,"datastore/expense_manager_app_data_store.preferences_pb").inputStream().use { androidx.datastore.preferences.PreferencesProto.PreferenceMap.parseFrom(it) }
        assertTrue(preferences.preferencesMap.getValue("jiangji_savings_plans_json").string.contains("500000"))
        assertArrayEquals(byteArrayOf(1,2,3,4),File(context.filesDir,"appearance/background").readBytes())
        assertTrue(File(context.noBackupFilesDir,"full-restore/rollback/database/expense_manager_database.db").exists())
        FullDataBackup.applyBeforeStartup(context)
        assertEquals(1, db.openHelper.writableDatabase.query("SELECT COUNT(*) FROM account").use { it.moveToFirst(); it.getInt(0) })
    }

    @Test fun traversalArchiveCannotTouchUserData() {
        val bytes = ByteArrayOutputStream().also { out -> ZipOutputStream(out).use { zip ->
            zip.putNextEntry(ZipEntry("files/../../outside")); zip.write(byteArrayOf(1)); zip.closeEntry()
        } }.toByteArray()
        assertTrue(runCatching { FullDataBackup.stage(context, ByteArrayInputStream(bytes)) }.isFailure)
        assertFalse(File(root,"outside").exists())
        assertFalse(File(context.noBackupFilesDir,"full-restore/pending").exists())
    }

    @Test fun selectedPhotoIsPortableEvenWhenOriginalProviderFileDisappears() {
        val base = ApplicationProvider.getApplicationContext<Context>()
        val photo = File(base.cacheDir,"captured_images/backup-fixture-${UUID.randomUUID()}.png").apply { parentFile!!.mkdirs() }
        val bitmap = android.graphics.Bitmap.createBitmap(20,20,android.graphics.Bitmap.Config.ARGB_8888)
        try { photo.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) } } finally { bitmap.recycle() }
        val bytes = photo.readBytes()
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(base,"${base.packageName}.fileprovider",photo)
            db.openHelper.writableDatabase.execSQL("UPDATE anxiety_relief_item SET image_uri=? WHERE id='comfort'",arrayOf(uri.toString()))
            val source = archive()
            photo.delete()
            FullDataBackup.stage(context,ByteArrayInputStream(source))
            db.close()
            FullDataBackup.applyBeforeStartup(context)
            db = open()
            val restored = db.openHelper.writableDatabase.query("SELECT image_uri FROM anxiety_relief_item WHERE id='comfort'").use { it.moveToFirst(); it.getString(0) }
            assertTrue(restored.startsWith("file://"))
            assertArrayEquals(bytes,File(android.net.Uri.parse(restored).path!!).readBytes())
        } finally { photo.delete() }
    }

    @Test fun duplicateImportIsRejectedUntilRestartAndDoesNotChangeActiveBalance() {
        val bytes = archive()
        FullDataBackup.stage(context, ByteArrayInputStream(bytes))
        assertTrue(runCatching { FullDataBackup.stage(context, ByteArrayInputStream(bytes)) }.isFailure)
        assertEquals(12345L, db.openHelper.writableDatabase.query("SELECT current_balance_cents FROM account WHERE id='a'").use { it.moveToFirst(); it.getLong(0) })
    }

    @Test fun unfinishedApplyRestoresSavedUserDataOnNextStartup() {
        val source = archive()
        FullDataBackup.stage(context, ByteArrayInputStream(source))
        db.openHelper.writableDatabase.execSQL("UPDATE account SET current_balance_cents=77777 WHERE id='a'")
        db.close()
        FullDataBackup.applyBeforeStartup(context)
        File(context.noBackupFilesDir,"full-restore/applying").writeText("simulated interruption")
        FullDataBackup.applyBeforeStartup(context)
        db = open()
        assertEquals(77777L, db.openHelper.writableDatabase.query("SELECT current_balance_cents FROM account WHERE id='a'").use { it.moveToFirst(); it.getLong(0) })
    }
}
