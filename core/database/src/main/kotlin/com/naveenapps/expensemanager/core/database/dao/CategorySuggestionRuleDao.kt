package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.CategorySuggestionRuleEntity
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface CategorySuggestionRuleDao {

    @Query(
        "SELECT * FROM category_suggestion_rule " +
            "WHERE source = :source AND ledger_id = :ledgerId ORDER BY updated_at DESC, id DESC",
    )
    fun observeRules(source: Int, ledgerId: String): Flow<List<CategorySuggestionRuleEntity>>

    fun observeRules(source: Int): Flow<List<CategorySuggestionRuleEntity>> = observeRules(source, DEFAULT_LEDGER_ID)

    @Query(
        "SELECT * FROM category_suggestion_rule " +
            "WHERE normalized_pattern = :normalizedPattern " +
            "AND category_id = :categoryId AND match_type = :matchType AND source = :source " +
            "AND ledger_id = :ledgerId " +
            "LIMIT 1",
    )
    suspend fun findRule(
        normalizedPattern: String,
        categoryId: String,
        matchType: Int,
        source: Int,
        ledgerId: String,
    ): CategorySuggestionRuleEntity?

    suspend fun findRule(
        normalizedPattern: String,
        categoryId: String,
        matchType: Int,
        source: Int,
    ): CategorySuggestionRuleEntity? = findRule(
        normalizedPattern,
        categoryId,
        matchType,
        source,
        DEFAULT_LEDGER_ID,
    )

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(rule: CategorySuggestionRuleEntity): Long

    @Query(
        "DELETE FROM category_suggestion_rule " +
            "WHERE normalized_pattern = :normalizedPattern AND match_type = :matchType " +
            "AND source = :source AND category_id != :categoryId AND ledger_id = :ledgerId",
    )
    suspend fun deleteConflictingRules(
        normalizedPattern: String,
        categoryId: String,
        matchType: Int,
        source: Int,
        ledgerId: String,
    ): Int

    suspend fun deleteConflictingRules(
        normalizedPattern: String,
        categoryId: String,
        matchType: Int,
        source: Int,
    ): Int = deleteConflictingRules(
        normalizedPattern,
        categoryId,
        matchType,
        source,
        DEFAULT_LEDGER_ID,
    )

    @Update
    suspend fun update(rule: CategorySuggestionRuleEntity)

    @Query("DELETE FROM category_suggestion_rule WHERE id = :id AND source = :source AND ledger_id = :ledgerId")
    suspend fun deleteUserRule(id: Long, source: Int, ledgerId: String): Int

    suspend fun deleteUserRule(id: Long, source: Int): Int = deleteUserRule(id, source, DEFAULT_LEDGER_ID)

    @Query("DELETE FROM category_suggestion_rule WHERE source = :source AND ledger_id = :ledgerId")
    suspend fun clearRules(source: Int, ledgerId: String): Int

    suspend fun clearRules(source: Int): Int = clearRules(source, DEFAULT_LEDGER_ID)
}
