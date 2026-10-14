package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        val categories = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            categoryDao.getCategories(ledgerId)
        } ?: categoryDao.getCategories()
        return categories.map { categories ->
            return@map categories?.map { it.toDomainModel() } ?: emptyList()
        }
    }

    override fun findCategoryFlow(categoryId: String): Flow<Category?> {
        val category = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            categoryDao.findByIdFlow(categoryId, ledgerId)
        } ?: categoryDao.findByIdFlow(categoryId)
        return category.map {
            it?.toDomainModel()
        }
    }

    override suspend fun getAllCategory(): Resource<List<Category>> = withContext(dispatchers.io) {
        return@withContext try {
            val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
            val category = categoryDao.getAllValues(ledgerId)

            if (category != null) {
                Resource.Success(category.map { it.toDomainModel() })
            } else {
                Resource.Error(KotlinNullPointerException())
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    override suspend fun findCategory(categoryId: String): Resource<Category> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val category = categoryDao.findById(categoryId, ledgerId)

                if (category != null) {
                    Resource.Success(category.toDomainModel())
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val response = categoryDao.insert(category.toEntityModel().copy(ledgerId = ledgerId))
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                categoryDao.update(category.toEntityModel().copy(ledgerId = ledgerId))
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteCategory(category: Category): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val response = categoryDao.delete(category.toEntityModel().copy(ledgerId = ledgerId))
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }
}
