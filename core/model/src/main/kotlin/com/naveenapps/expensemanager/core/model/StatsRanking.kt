package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

/** The two ranking modes available on the Stats page. */
@Stable
enum class RankingMode {
    EXPENSE,
    INCOME,
}

/** Sorting applied to transactions inside a category drill-down. */
@Stable
enum class RankingSortMode {
    AMOUNT,
    TIME,
}

/** A category aggregate returned by the Stats ranking use case. */
@Stable
data class CategoryRankingItem(
    val categoryId: String,
    val categoryName: String,
    val categoryTitleResId: Int?,
    val categoryIcon: StoredIcon,
    val totalAmountCents: Long,
    val totalAmount: String,
    val percentage: Double,
    val previousMonthAmountCents: Long? = null,
    val monthChangeCents: Long? = null,
    val monthChangeAmount: String? = null,
)

/** A transaction row returned by a category drill-down. */
@Stable
data class CategoryTransactionRankingItem(
    val transactionId: String,
    val displayName: String,
    val categoryName: String,
    val categoryTitleResId: Int?,
    val categoryIcon: StoredIcon,
    val amountCents: Long,
    val amount: String,
    val percentage: Double,
    val createdOn: Date,
)
