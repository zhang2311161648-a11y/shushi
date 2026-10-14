package com.naveenapps.expensemanager.ui

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.data.repository.AnxietyReliefRepositoryImpl
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AnxietyItemDeletionDatabaseTest {
    @Test fun deletionPersistsAfterReopeningAndKeepsOtherItems() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val databaseName = "anxiety-delete-${UUID.randomUUID()}.db"
        fun openDatabase() = Room.databaseBuilder(context, ExpenseManagerDatabase::class.java, databaseName).build()
        var database = openDatabase()
        val dispatchers = AppCoroutineDispatchers(Dispatchers.IO, Dispatchers.IO, Dispatchers.IO)
        val original = AnxietyReliefItem("delete", "测试物品", null, 1000, "2026-10-01", 500, "", 1, 1)
        val keep = original.copy(id = "keep", name = "保留物品")
        try {
            database.anxietyReliefItemDao().insert(keep.toEntityModel())
            for (item in listOf(original, original.copy(isArchived = true), original.copy(isSold = true))) {
                database.anxietyReliefItemDao().insert(item.toEntityModel())
                val repository = AnxietyReliefRepositoryImpl(database.anxietyReliefItemDao(), dispatchers)
                assertEquals(Resource.Success(true), repository.delete(item))
                assertEquals(Resource.Success(false), repository.delete(item))
                database.close()
                database = openDatabase()
                assertNull(database.anxietyReliefItemDao().findById(item.id))
                assertEquals(keep.toEntityModel(), database.anxietyReliefItemDao().findById(keep.id))
            }
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
