package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.CategoryRankingRow
import com.naveenapps.expensemanager.core.database.entity.CategoryTransactionRankingRow
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.StoredIcon

fun CategoryRankingRow.toDomainModel(): CategoryRankingItem {
    return CategoryRankingItem(
        categoryId = categoryId,
        categoryName = categoryName,
        categoryTitleResId = defaultCategoryKey.toCategoryTitleResId(),
        categoryIcon = StoredIcon(
            name = iconName,
            backgroundColor = iconBackgroundColor,
            customImagePath = customImagePath,
        ),
        totalAmountCents = totalAmountCents,
        totalAmount = "",
        percentage = 0.0,
    )
}

fun CategoryTransactionRankingRow.toDomainModel(): CategoryTransactionRankingItem {
    return CategoryTransactionRankingItem(
        transactionId = transactionId,
        displayName = notes.trim().takeIf { it.isNotEmpty() } ?: categoryName,
        categoryName = categoryName,
        categoryTitleResId = defaultCategoryKey.toCategoryTitleResId(),
        categoryIcon = StoredIcon(
            name = iconName,
            backgroundColor = iconBackgroundColor,
            customImagePath = customImagePath,
        ),
        amountCents = amountCents,
        amount = "",
        percentage = 0.0,
        createdOn = createdOn,
    )
}
