package com.naveenapps.expensemanager.feature.agent
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.MIGRATION_17_18

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration17To18DeviceTest {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),ExpenseManagerDatabase::class.java)
    @Test fun onlyAddsDraftStorageAndPreservesAllExistingRows() {
        val before = helper.createDatabase("ai_migration_fixture_17_18",17)
        before.execSQL("INSERT INTO account (id,name,type,icon_background_color,icon_name,amount,credit_limit,sequence,created_on,updated_on,custom_image_path,initial_balance_cents,current_balance_cents,ledger_id) VALUES ('fixture-wallet','fixture',0,'#000000','wallet',123.45,0,0,1000,1000,NULL,12345,12345,'default')")
        val hashes = linkedMapOf<String,List<String>>()
        before.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT IN ('room_master_table','android_metadata')").use { cursor ->
            while(cursor.moveToNext()) {
                val name = cursor.getString(0)
                hashes[name] = before.query("SELECT * FROM `$name`").use { rows -> buildList { while(rows.moveToNext()) add((0 until rows.columnCount).joinToString("|") { rows.getString(it) ?: "<null>" }) } }
            }
        }
        before.close()
        val after = helper.runMigrationsAndValidate("ai_migration_fixture_17_18",18,true,MIGRATION_17_18)
        hashes.forEach { (table, expected) ->
            val actual = after.query("SELECT * FROM `$table`").use { rows -> buildList { while(rows.moveToNext()) add((0 until rows.columnCount).joinToString("|") { rows.getString(it) ?: "<null>" }) } }
            assertEquals(table,expected,actual)
        }
        after.query("SELECT * FROM bookkeeping_draft_batch").use { assertEquals(0,it.count) }
        after.close()
    }
}

