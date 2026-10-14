package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.naveenapps.expensemanager.core.database.entity.CategoryRankingRow
import com.naveenapps.expensemanager.core.database.entity.CategoryTransactionRankingRow
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsRankingDao {

    @Query(
        """
        SELECT
            c.id AS category_id,
            c.name AS category_name,
            c.icon_name AS icon_name,
            c.icon_background_color AS icon_background_color,
            c.custom_image_path AS custom_image_path,
            c.default_category_key AS default_category_key,
            SUM(t.base_amount_cents) AS total_amount_cents
        FROM `transaction` AS t
        INNER JOIN category AS c ON c.id = t.category_id
        WHERE t.created_on >= :startDate
          AND t.created_on < :endDate
          AND t.type = :transactionType
          AND t.ledger_id = :ledgerId
          AND c.ledger_id = :ledgerId
        GROUP BY c.id, c.name, c.icon_name, c.icon_background_color,
                 c.custom_image_path, c.default_category_key
        ORDER BY total_amount_cents DESC
        """,
    )
    fun getCategoryRanking(
        transactionType: Int,
        startDate: Long,
        endDate: Long,
        ledgerId: String,
    ): Flow<List<CategoryRankingRow>>

    fun getCategoryRanking(
        transactionType: Int,
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryRankingRow>> = getCategoryRanking(
        transactionType,
        startDate,
        endDate,
        DEFAULT_LEDGER_ID,
    )

    @Query(
        """
        SELECT
            t.id AS transaction_id,
            t.notes AS notes,
            t.base_amount_cents AS amount_cents,
            t.created_on AS created_on,
            c.name AS category_name,
            c.icon_name AS icon_name,
            c.icon_background_color AS icon_background_color,
            c.custom_image_path AS custom_image_path,
            c.default_category_key AS default_category_key
        FROM `transaction` AS t
        INNER JOIN category AS c ON c.id = t.category_id
        WHERE t.category_id = :categoryId
          AND t.created_on >= :startDate
          AND t.created_on < :endDate
          AND t.type = :transactionType
          AND t.ledger_id = :ledgerId
          AND c.ledger_id = :ledgerId
        ORDER BY t.base_amount_cents DESC, t.created_on DESC
        """,
    )
    fun getTransactionsByCategoryByAmount(
        categoryId: String,
        transactionType: Int,
        startDate: Long,
        endDate: Long,
        ledgerId: String,
    ): Flow<List<CategoryTransactionRankingRow>>

    fun getTransactionsByCategoryByAmount(
        categoryId: String,
        transactionType: Int,
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryTransactionRankingRow>> = getTransactionsByCategoryByAmount(
        categoryId,
        transactionType,
        startDate,
        endDate,
        DEFAULT_LEDGER_ID,
    )

    @Query(
        """
        SELECT
            t.id AS transaction_id,
            t.notes AS notes,
            t.base_amount_cents AS amount_cents,
            t.created_on AS created_on,
            c.name AS category_name,
            c.icon_name AS icon_name,
            c.icon_background_color AS icon_background_color,
            c.custom_image_path AS custom_image_path,
            c.default_category_key AS default_category_key
        FROM `transaction` AS t
        INNER JOIN category AS c ON c.id = t.category_id
        WHERE t.category_id = :categoryId
          AND t.created_on >= :startDate
          AND t.created_on < :endDate
          AND t.type = :transactionType
          AND t.ledger_id = :ledgerId
          AND c.ledger_id = :ledgerId
        ORDER BY t.created_on DESC, amount_cents DESC
        """,
    )
    fun getTransactionsByCategoryByTime(
        categoryId: String,
        transactionType: Int,
        startDate: Long,
        endDate: Long,
        ledgerId: String,
    ): Flow<List<CategoryTransactionRankingRow>>

    fun getTransactionsByCategoryByTime(
        categoryId: String,
        transactionType: Int,
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategoryTransactionRankingRow>> = getTransactionsByCategoryByTime(
        categoryId,
        transactionType,
        startDate,
        endDate,
        DEFAULT_LEDGER_ID,
    )
}
