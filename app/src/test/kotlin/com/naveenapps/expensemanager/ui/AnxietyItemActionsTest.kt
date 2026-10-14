package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModelStore
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.*
import com.naveenapps.expensemanager.core.domain.usecase.settings.currency.GetCurrencyUseCase
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AnxietyReliefRepository
import com.naveenapps.expensemanager.core.repository.AnxietyReliefSettingsRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AnxietyItemActionsTest {
    private val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    private val item = AnxietyReliefItem("camera", "相机", null, 10000, "2026-10-01", 500, "备注", 1, 1, costByUse = true, useCount = 3)
    private val repository = ItemRepository()
    private lateinit var viewModel: JiangJiAnxietyReliefViewModel

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        val settings = mock<AnxietyReliefSettingsRepository>()
        whenever(settings.getDefaultDailyTargetCents()).thenReturn(flowOf(500L))
        val currency = mock<CurrencyRepository>()
        whenever(currency.getSelectedCurrency()).thenReturn(flowOf(Currency("¥", "人民币")))
        viewModel = JiangJiAnxietyReliefViewModel(
            GetAnxietyReliefItemsUseCase(repository), GetAnxietyReliefDefaultTargetUseCase(settings),
            GetCurrencyUseCase(currency), AddAnxietyReliefItemUseCase(repository),
            UpdateAnxietyReliefItemUseCase(repository), ArchiveAnxietyReliefItemUseCase(repository),
            DeleteAnxietyReliefItemUseCase(repository), SetAnxietyReliefDefaultTargetUseCase(settings),
        )
        store.put("test", viewModel)
    }

    @After fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test fun activeArchivedAndLegacySoldItemsCanBeEditedWithoutLosingTheirIdentity() = runTest(dispatcher) {
        listOf(item, item.copy(isArchived = true), item.copy(isSold = true)).forEach { original ->
            repository.records.value = listOf(original)
            advanceUntilIdle()
            viewModel.openEdit(original)
            assertEquals(if (original.isArchived || original.isSold) "已归档" else "陪伴中", viewModel.state.value.editor?.status)
            assertEquals(if (original.isArchived || original.isSold) "已归档" else "陪伴中", original.statusLabel())
            viewModel.updateName("更新相机")
            viewModel.updatePrice("120.50")
            viewModel.saveEditor()
            advanceUntilIdle()
            val saved = repository.records.value.single()
            assertEquals(original.id, saved.id)
            assertEquals(original.createdAt, saved.createdAt)
            assertEquals(original.useCount, saved.useCount)
            assertEquals("更新相机", saved.name)
            assertEquals(12050L, saved.priceCents)
            assertEquals(original.isArchived || original.isSold, saved.isArchived)
            assertFalse(saved.isSold)
            assertNull(viewModel.state.value.editor)
        }
    }

    @Test fun deletingEachStatusRemovesOnlyTheSelectedItem() = runTest(dispatcher) {
        val others = item.copy(id = "keep", name = "保留物品")
        listOf(item, item.copy(isArchived = true), item.copy(isSold = true)).forEach { original ->
            repository.records.value = listOf(original, others)
            advanceUntilIdle()
            var completed = false
            viewModel.delete(original) { completed = true }
            advanceUntilIdle()
            assertTrue(completed)
            assertEquals(listOf(others), repository.records.value)
            assertEquals(listOf(others), viewModel.state.value.items)
            assertFalse(viewModel.state.value.isDeleting)
        }
    }

    @Test fun failedDeletionKeepsTheItemAndAllowsRetry() = runTest(dispatcher) {
        repository.records.value = listOf(item)
        advanceUntilIdle()
        for (failure in listOf(Resource.Success(false), Resource.Error(Exception("write failed")))) {
            repository.deleteResult = failure
            var completed = false
            viewModel.delete(item) { completed = true }
            advanceUntilIdle()
            assertFalse(completed)
            assertEquals(listOf(item), repository.records.value)
            assertEquals(listOf(item), viewModel.state.value.items)
            assertEquals("删除失败，请重试", viewModel.state.value.errorMessage)
            assertFalse(viewModel.state.value.isDeleting)
        }
        repository.deleteResult = Resource.Success(true)
        viewModel.delete(item)
        advanceUntilIdle()
        assertTrue(repository.records.value.isEmpty())
        assertNull(viewModel.state.value.errorMessage)
    }

    private class ItemRepository : AnxietyReliefRepository {
        val records = MutableStateFlow<List<AnxietyReliefItem>>(emptyList())
        var deleteResult: Resource<Boolean> = Resource.Success(true)
        override fun getItems() = records
        override suspend fun add(item: AnxietyReliefItem): Resource<Boolean> {
            records.value += item
            return Resource.Success(true)
        }
        override suspend fun update(item: AnxietyReliefItem): Resource<Boolean> {
            records.value = records.value.map { if (it.id == item.id) item else it }
            return Resource.Success(true)
        }
        override suspend fun archive(item: AnxietyReliefItem) = update(item.copy(isArchived = true))
        override suspend fun delete(item: AnxietyReliefItem): Resource<Boolean> {
            if ((deleteResult as? Resource.Success)?.data == true) records.value = records.value.filterNot { it.id == item.id }
            return deleteResult
        }
    }
}
