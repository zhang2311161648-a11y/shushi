package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.CategorySuggestionRuleDao
import com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule
import com.naveenapps.expensemanager.core.model.CategorySuggestionRuleSource
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.model.normalizeSuggestionText
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CategorySuggestionRuleRepositoryImpl(
    private val dao: CategorySuggestionRuleDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : CategorySuggestionRuleRepository {

    override fun observeUserRules(): Flow<List<CategorySuggestionRule>> =
        (currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            dao.observeRules(CategorySuggestionRuleSource.USER_LEARNED.ordinal, ledgerId)
        } ?: dao.observeRules(CategorySuggestionRuleSource.USER_LEARNED.ordinal)).map { rules ->
            rules.mapNotNull { it.toDomainModel() }
        }

    override suspend fun saveUserRule(
        pattern: String,
        categoryId: String,
        matchType: CategorySuggestionMatchType,
    ): Resource<Boolean> = withContext(dispatchers.io) {
        val normalizedPattern = normalizeSuggestionText(pattern)
        if (normalizedPattern.isBlank() || categoryId.isBlank()) {
            return@withContext Resource.Error(IllegalArgumentException("A rule needs text and a category"))
        }

        return@withContext try {
            val source = CategorySuggestionRuleSource.USER_LEARNED
            val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
            val now = System.currentTimeMillis()
            dao.deleteConflictingRules(
                normalizedPattern = normalizedPattern,
                categoryId = categoryId,
                matchType = matchType.ordinal,
                source = source.ordinal,
                ledgerId = ledgerId,
            )
            val existing = dao.findRule(
                normalizedPattern = normalizedPattern,
                categoryId = categoryId,
                matchType = matchType.ordinal,
                source = source.ordinal,
                ledgerId = ledgerId,
            )
            if (existing == null) {
                dao.insert(
                    CategorySuggestionRule(
                        pattern = pattern.trim(),
                        normalizedPattern = normalizedPattern,
                        categoryId = categoryId,
                        matchType = matchType,
                        source = source,
                        createdAt = now,
                        updatedAt = now,
                        lastUsedAt = now,
                    ).toEntityModel().copy(ledgerId = ledgerId),
                )
            } else {
                dao.update(
                    existing.copy(
                        pattern = pattern.trim(),
                        usageCount = existing.usageCount + 1,
                        updatedAt = now,
                        lastUsedAt = now,
                    ),
                )
            }
            Resource.Success(true)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun deleteUserRule(id: Long): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            Resource.Success(
                dao.deleteUserRule(
                    id,
                    CategorySuggestionRuleSource.USER_LEARNED.ordinal,
                    currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID,
                ) > 0,
            )
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun clearUserRules(): Resource<Boolean> = withContext(dispatchers.io) {
        return@withContext try {
            dao.clearRules(
                CategorySuggestionRuleSource.USER_LEARNED.ordinal,
                currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID,
            )
            Resource.Success(true)
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }
}
