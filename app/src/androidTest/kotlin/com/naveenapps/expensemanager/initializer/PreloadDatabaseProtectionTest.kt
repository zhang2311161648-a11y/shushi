package com.naveenapps.expensemanager.initializer

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnit4
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreloadDatabaseProtectionTest {
    private lateinit var database: ExpenseManagerDatabase

    @Before fun openDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ExpenseManagerDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After fun closeDatabase() = database.close()

    @Test fun emptyDatabaseCanBeSeeded() {
        assertFalse(hasExistingUserData(database.openHelper.writableDatabase))
    }

    @Test fun wellbeingRecordAlonePreventsReseeding() {
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("INSERT INTO anxiety_relief_item VALUES ('old-item', '旧物品', NULL, 10000, '2026-01-01', 50, '', 0, 0, 0)")
        assertTrue(hasExistingUserData(sqlite))
    }

    @Test fun savingsAlonePreventsReseeding() {
        val sqlite = database.openHelper.writableDatabase
        sqlite.execSQL("INSERT INTO monthly_saving (year, month, amount_cents, created_at, updated_at) VALUES (2026, 1, 5000, 0, 0)")
        assertTrue(hasExistingUserData(sqlite))
    }
}
