package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration18To19Test {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), ExpenseManagerDatabase::class.java)

    @Test fun oldItemsRetainTheirDataAndReceiveSafeDefaults() {
        val name = "anxiety-migration-18-19"
        helper.createDatabase(name, 18).apply {
            execSQL("INSERT INTO anxiety_relief_item (id,name,image_uri,price_cents,purchase_date,target_daily_cost_cents,note,created_at,updated_at,is_archived) VALUES ('old','相机','photo.jpg',749900,'2024-01-02',500,'[mood:开心]原有备注',123,456,1)")
            close()
        }
        helper.runMigrationsAndValidate(name, 19, true, MIGRATION_18_19).use { db ->
            db.query("SELECT * FROM anxiety_relief_item WHERE id='old'").use { c ->
                assertTrue(c.moveToFirst())
                assertEquals("相机", c.getString(c.getColumnIndexOrThrow("name")))
                assertEquals(749900L, c.getLong(c.getColumnIndexOrThrow("price_cents")))
                assertEquals("[mood:开心]原有备注", c.getString(c.getColumnIndexOrThrow("note")))
                assertEquals(1, c.getInt(c.getColumnIndexOrThrow("is_archived")))
                assertEquals("未分类", c.getString(c.getColumnIndexOrThrow("category")))
                listOf("is_sold", "cost_by_use", "use_count").forEach { assertEquals(0L, c.getLong(c.getColumnIndexOrThrow(it))) }
            }
        }
    }
}
