package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.datastore.preferences.PreferencesProto
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Complete local snapshot. Restore runs before Room, DataStore and preferences are opened. */
object FullDataBackup {
    private const val DATABASE = "expense_manager_database.db"
    private const val FORMAT = "shushi-full-backup"
    private const val MAX_BYTES = 2L * 1024 * 1024 * 1024
    private fun root(context: Context) = File(context.noBackupFilesDir, "full-restore")
    fun consumeRestoreMessage(context: Context): String? {
        val messages = listOf(File(root(context),"restore-success.txt"),File(root(context),"restore-failed.txt"))
        val latest = messages.filter { it.isFile }.maxByOrNull { it.lastModified() } ?: return null
        val result = if (latest.name == "restore-success.txt") "全部数据恢复成功，恢复前副本已保留" else "完整备份恢复失败，已保留或恢复原数据"
        messages.forEach { it.delete() }
        return result
    }

    fun export(context: Context, database: ExpenseManagerDatabase, output: java.io.OutputStream) {
        val work = File(context.cacheDir, "full-export-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            val snapshot = File(work, DATABASE)
            // SQLite's VACUUM INTO reads a consistent committed snapshot, including WAL content.
            database.openHelper.writableDatabase.execSQL("VACUUM INTO '${snapshot.absolutePath.replace("'", "''")}'")
            validateDatabase(snapshot)
            val files = linkedMapOf("database/$DATABASE" to snapshot)
            context.filesDir.walkTopDown().filter { it.isFile && !it.name.endsWith(".tmp") && !it.name.endsWith(".lock") }.forEach {
                files["files/${it.relativeTo(context.filesDir).invariantSeparatorsPath}"] = it
            }
            val prefs = File(context.applicationInfo.dataDir, "shared_prefs")
            prefs.listFiles()?.filter { it.extension == "xml" }?.forEach {
                // Flush in-memory SharedPreferences before snapshotting their XML.
                check(context.getSharedPreferences(it.nameWithoutExtension, Context.MODE_PRIVATE).edit().commit())
                files["preferences/${it.name}"] = it
            }
            includeReferencedImages(context, snapshot, work, files)
            validateDatabase(snapshot)
            val manifest = JSONObject().put("format", FORMAT).put("version", 1)
                .put("package", context.packageName).put("sourceRoot", context.applicationInfo.dataDir)
                .put("createdAt", System.currentTimeMillis()).put("databaseVersion", 18)
            val entries = JSONObject()
            // Stage every file once, so checksums and ZIP bytes refer to the same content.
            files.entries.toList().forEachIndexed { index, entry ->
                val staged = File(work, "entry-$index")
                entry.value.copyTo(staged)
                files[entry.key] = staged
                entries.put(entry.key, JSONObject().put("bytes", staged.length()).put("sha256", sha(staged)))
            }
            manifest.put("entries", entries)
            ZipOutputStream(output).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString().toByteArray(Charsets.UTF_8)); zip.closeEntry()
                files.forEach { (name, file) ->
                    zip.putNextEntry(ZipEntry(name)); file.inputStream().use { it.copyTo(zip) }; zip.closeEntry()
                }
            }
        } finally { work.deleteRecursively() }
    }

    /** Validate into private staging; never touch active user data during import selection. */
    fun stage(context: Context, input: java.io.InputStream): Int {
        val directory = File(root(context), "incoming-${UUID.randomUUID()}").apply { mkdirs() }
        try {
            extract(input, directory)
            val manifest = validate(context, directory)
            val db = File(directory, "database/$DATABASE")
            val count = SQLiteDatabase.openDatabase(db.path, null, SQLiteDatabase.OPEN_READONLY).use {
                it.rawQuery("SELECT COUNT(*) FROM `transaction`", null).use { cursor -> cursor.moveToFirst(); cursor.getInt(0) }
            }
            remap(directory, manifest.getString("sourceRoot"), context.applicationInfo.dataDir)
            val checkedEntries = manifest.getJSONObject("entries")
            checkedEntries.keys().forEach { name ->
                val file = File(directory,name)
                checkedEntries.put(name,JSONObject().put("bytes",file.length()).put("sha256",sha(file)))
            }
            manifest.put("sourceRoot",context.applicationInfo.dataDir)
            File(directory,"manifest.json").writeText(manifest.toString())
            val pending = File(root(context), "pending")
            check(!pending.exists()) { "已有待恢复备份，请先退出应用并重开" }
            check(directory.renameTo(pending)) { "无法保存待恢复备份" }
            return count
        } catch (error: Throwable) { directory.deleteRecursively(); throw error }
    }

    /** Called from Application.attachBaseContext, before AndroidX startup providers. */
    fun applyBeforeStartup(context: Context) {
        val storage = root(context)
        val pending = File(storage, "pending")
        val rollback = File(storage, "rollback")
        val journal = File(storage, "applying")
        if (journal.exists()) {
            restoreRollback(context, rollback)
            journal.delete()
            if (pending.exists()) pending.renameTo(File(storage, "interrupted-${System.currentTimeMillis()}"))
            File(storage, "restore-failed.txt").writeText("上次恢复未完成，已恢复原数据。")
            return
        }
        if (!pending.exists()) return
        // Staged files were remapped, so original checksums no longer apply. Validate the DB again.
        try { validate(context,pending) } catch (error: Exception) {
            pending.renameTo(File(storage, "invalid-${System.currentTimeMillis()}"))
            File(storage, "restore-failed.txt").writeText("待恢复备份校验失败，原数据未修改。")
            return
        }
        if (rollback.exists()) {
            check(rollback.renameTo(File(storage, "rollback-${System.currentTimeMillis()}")))
        }
        rollback.mkdirs()
        val targets = targets(context, pending)
        val absent = mutableListOf<String>()
        targets.forEach { (name, target) ->
            if (target.exists()) target.copyTo(File(rollback, name).apply { parentFile!!.mkdirs() })
            else absent += name
        }
        // Save WAL alongside the old DB; no database has been opened in this new process yet.
        listOf("-wal", "-shm").forEach { suffix ->
            val old = File(context.getDatabasePath(DATABASE).path + suffix)
            if (old.exists()) old.copyTo(File(rollback, "database/$DATABASE$suffix").apply { parentFile!!.mkdirs() })
        }
        File(rollback, "absent.json").writeText(org.json.JSONArray(absent).toString())
        File(rollback, "targets.json").writeText(org.json.JSONArray(targets.keys.toList()).toString())
        journal.writeText("Restore in progress; retain rollback until completion")
        try {
            targets.forEach { (name, target) -> atomicCopy(File(pending, name), target) }
            listOf("-wal", "-shm").forEach { File(context.getDatabasePath(DATABASE).path + it).delete() }
            validateDatabase(context.getDatabasePath(DATABASE))
            check(pending.renameTo(File(storage, "imported-${System.currentTimeMillis()}")))
            journal.delete()
            File(storage, "restore-success.txt").writeText("完整备份恢复成功；恢复前副本保留在应用私有目录。")
        } catch (error: Throwable) {
            android.util.Log.e("ShushiBackup", "Full restore failed; rolling back", error)
            restoreRollback(context, rollback)
            journal.delete()
            pending.renameTo(File(storage, "failed-${System.currentTimeMillis()}"))
            File(storage, "restore-failed.txt").writeText("恢复失败，已恢复原数据：${error.javaClass.simpleName}: ${error.message}")
        }
    }

    private fun targets(context: Context, directory: File): Map<String, File> = directory.walkTopDown()
        .filter { it.isFile && allowed(it.relativeTo(directory).invariantSeparatorsPath) }
        .associate { val name = it.relativeTo(directory).invariantSeparatorsPath; name to target(context, name) }

    private fun target(context: Context, name: String): File = when {
        name.startsWith("database/") -> context.getDatabasePath(name.removePrefix("database/"))
        name.startsWith("files/") -> File(context.filesDir, name.removePrefix("files/"))
        name.startsWith("preferences/") -> File(File(context.applicationInfo.dataDir, "shared_prefs"), name.removePrefix("preferences/"))
        else -> error("非法备份路径")
    }

    private fun restoreRollback(context: Context, rollback: File) {
        val names = org.json.JSONArray(File(rollback, "targets.json").readText())
        val absent = org.json.JSONArray(File(rollback, "absent.json").readText())
        repeat(names.length()) { index ->
            val name = names.getString(index)
            val saved = File(rollback, name)
            if (saved.exists()) atomicCopy(saved, target(context, name))
        }
        repeat(absent.length()) { target(context, absent.getString(it)).delete() }
        listOf("-wal", "-shm").forEach { suffix ->
            val saved = File(rollback, "database/$DATABASE$suffix")
            val destination = File(context.getDatabasePath(DATABASE).path + suffix)
            if (saved.exists()) atomicCopy(saved, destination) else destination.delete()
        }
    }

    private fun atomicCopy(source: File, destination: File) {
        destination.parentFile!!.mkdirs()
        val temporary = File(destination.parentFile, "${destination.name}.restore-tmp")
        source.inputStream().use { input -> temporary.outputStream().use { output -> input.copyTo(output); output.fd.sync() } }
        check(temporary.renameTo(destination)) { "无法替换备份文件" }
    }

    private fun allowed(name: String): Boolean {
        if (name.contains('\\') || name.startsWith('/') || name.split('/').any { it == ".." || it.isEmpty() }) return false
        return name == "database/$DATABASE" || name.startsWith("files/") ||
            (name.startsWith("preferences/") && name.count { it == '/' } == 1 && name.endsWith(".xml"))
    }

    private fun extract(input: java.io.InputStream, directory: File) {
        var total = 0L; var count = 0
        val seen = mutableSetOf<String>()
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                check(!entry.isDirectory && (entry.name == "manifest.json" || allowed(entry.name))) { "非法备份内容" }
                check(seen.add(entry.name) && ++count <= 20000) { "重复或过多备份文件" }
                val file = File(directory, entry.name).apply { parentFile!!.mkdirs() }
                file.outputStream().use { output ->
                    val buffer = ByteArray(65536)
                    while (true) { val n = zip.read(buffer); if (n < 0) break; total += n; check(total <= MAX_BYTES); output.write(buffer, 0, n) }
                    output.fd.sync()
                }
                zip.closeEntry()
            }
        }
    }

    private fun validate(context: Context, directory: File): JSONObject {
        val manifestFile = File(directory, "manifest.json")
        check(manifestFile.length() in 1..8_000_000) { "缺少备份清单" }
        val manifest = JSONObject(manifestFile.readText())
        check(manifest.getString("format") == FORMAT && manifest.getInt("version") == 1) { "不支持的备份格式" }
        check(manifest.getString("package") == context.packageName) { "请选择书史同一应用的完整备份" }
        val entries = manifest.getJSONObject("entries")
        check(entries.has("database/$DATABASE"))
        val actual = directory.walkTopDown().filter { it.isFile && it.relativeTo(directory).invariantSeparatorsPath != "manifest.json" }.map { it.relativeTo(directory).invariantSeparatorsPath }.toSet()
        check(actual == entries.keys().asSequence().toSet()) { "备份文件清单不匹配" }
        entries.keys().forEach { name ->
            check(allowed(name)); val file = File(directory, name); val info = entries.getJSONObject(name)
            check(file.length() == info.getLong("bytes") && sha(file) == info.getString("sha256")) { "备份完整性校验失败" }
        }
        validateDatabase(File(directory, "database/$DATABASE"))
        File(directory,"files/datastore").listFiles()?.filter { it.name.endsWith(".preferences_pb") }?.forEach {
            check(it.length() <= 16L * 1024 * 1024)
            it.inputStream().use { input -> PreferencesProto.PreferenceMap.parseFrom(input) }
        }
        return manifest
    }

    private fun includeReferencedImages(context: Context, snapshot: File, work: File, files: MutableMap<String,File>) {
        val replacements = linkedMapOf<String,String>()
        fun copyReference(uri: String): String {
            return replacements.getOrPut(uri) {
                val name = "backup-images/${UUID.randomUUID()}.image"
                val staged = File(work, "image-${UUID.randomUUID()}")
                context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { input ->
                    staged.outputStream().use { output -> input.copyTo(output) }
                } ?: error("有已选择图片无法读取，完整备份未生成；请重新选择该图片后重试")
                check(staged.length() in 1..100_000_000) { "已选择图片过大或为空" }
                files["files/$name"] = staged
                android.net.Uri.fromFile(File(context.filesDir,name)).toString()
            }
        }
        SQLiteDatabase.openDatabase(snapshot.path,null,SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.rawQuery("PRAGMA journal_mode=DELETE",null).use { it.moveToFirst() }
            val tables = mutableListOf<String>()
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'",null).use { while(it.moveToNext()) tables += it.getString(0) }
            tables.forEach { table ->
                val columns = mutableListOf<String>()
                db.rawQuery("PRAGMA table_info(`${table.replace("`","``")}`)",null).use { while(it.moveToNext()) if(it.getString(1) in listOf("image_uri","image_path","custom_image_path")) columns += it.getString(1) }
                columns.forEach { column ->
                    val references = mutableListOf<String>()
                    db.rawQuery("SELECT DISTINCT `$column` FROM `${table.replace("`","``")}` WHERE `$column` LIKE 'content://%'",null).use { while(it.moveToNext()) references += it.getString(0) }
                    references.forEach { old ->
                        val uri = copyReference(old)
                        val replacement = if (column == "image_uri") uri else android.net.Uri.parse(uri).path!!
                        db.execSQL("UPDATE `${table.replace("`","``")}` SET `$column`=? WHERE `$column`=?",arrayOf(replacement,old))
                    }
                }
            }
        }
        // Stage preference files before rewriting them; never mutate active settings while exporting.
        files.entries.toList().filter { it.key.endsWith(".preferences_pb") || it.key.startsWith("preferences/") }.forEach { (name,source) ->
            val staged = File(work,"settings-${UUID.randomUUID()}")
            source.copyTo(staged)
            if(name.endsWith(".preferences_pb")) {
                val map = staged.inputStream().use { PreferencesProto.PreferenceMap.parseFrom(it) }
                val builder = map.toBuilder()
                map.preferencesMap.forEach { (key,value) ->
                    if(value.hasString() && value.string.startsWith("content://")) builder.putPreferences(key,value.toBuilder().setString(copyReference(value.string)).build())
                }
                staged.outputStream().use { builder.build().writeTo(it) }
            } else {
                var xml = staged.readText()
                replacements.forEach { (old,new) -> xml = xml.replace(old.replace("&","&amp;"),new) }
                staged.writeText(xml)
            }
            files[name] = staged
        }
    }

    private fun validateDatabase(file: File) {
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            check(db.version in 18..19) { "该完整备份的数据库版本不兼容" }
            db.rawQuery("SELECT identity_hash FROM room_master_table WHERE id=42", null).use {
                check(it.moveToFirst() && it.getString(0) == (if (db.version == 18) "cc1f03d5ce85562697802d5f85f248c4" else "d56428b7b6c04b889746a3e93960de2c")) { "备份数据库结构不兼容" }
            }
            db.rawQuery("PRAGMA integrity_check", null).use { check(it.moveToFirst() && it.getString(0) == "ok") { "备份数据库已损坏" } }
            db.rawQuery("PRAGMA foreign_key_check", null).use { check(!it.moveToFirst()) { "备份关联数据不完整" } }
            val required = listOf("ledger", "account", "category", "transaction", "budget", "anxiety_relief_item", "monthly_saving", "bookkeeping_draft_batch")
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { cursor ->
                val names = buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }; check(names.containsAll(required)) { "备份缺少必需数据表" }
            }
        }
    }

    private fun remap(directory: File, oldRoot: String, newRoot: String) {
        if (oldRoot == newRoot) return
        check(oldRoot.startsWith("/data/") && oldRoot.length > 10)
        File(directory, "preferences").listFiles()?.forEach { it.writeText(it.readText().replace(oldRoot, newRoot)) }
        File(directory, "files/datastore").listFiles()?.filter { it.name.endsWith(".preferences_pb") }?.forEach { file ->
            val map = file.inputStream().use { PreferencesProto.PreferenceMap.parseFrom(it) }
            val builder = map.toBuilder()
            map.preferencesMap.forEach { (key, value) ->
                if (value.hasString()) builder.putPreferences(key, value.toBuilder().setString(value.string.replace(oldRoot, newRoot)).build())
            }
            file.outputStream().use { builder.build().writeTo(it) }
        }
        SQLiteDatabase.openDatabase(File(directory, "database/$DATABASE").path, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            val tables = mutableListOf<String>()
            db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null).use { while (it.moveToNext()) tables += it.getString(0) }
            db.beginTransaction()
            try {
                tables.forEach { table ->
                    val columns = mutableListOf<String>()
                    db.rawQuery("PRAGMA table_info(`${table.replace("`", "``")}`)", null).use { while (it.moveToNext()) if (it.getString(2).uppercase() == "TEXT") columns += it.getString(1) }
                    columns.forEach { column -> db.execSQL("UPDATE `${table.replace("`", "``")}` SET `${column.replace("`", "``")}`=replace(`${column.replace("`", "``")}`, ?, ?) WHERE instr(`${column.replace("`", "``")}`, ?) > 0", arrayOf(oldRoot, newRoot, oldRoot)) }
                }
                db.setTransactionSuccessful()
            } finally { db.endTransaction() }
        }
    }

    private fun sha(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input -> val bytes = ByteArray(65536); while (true) { val n = input.read(bytes); if (n < 0) break; digest.update(bytes, 0, n) } }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
