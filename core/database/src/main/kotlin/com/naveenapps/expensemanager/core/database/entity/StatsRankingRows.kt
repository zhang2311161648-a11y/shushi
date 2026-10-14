package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo

data class CategoryRankingRow(
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "custom_image_path")
    val customImagePath: String?,
    @ColumnInfo(name = "default_category_key")
    val defaultCategoryKey: String?,
    @ColumnInfo(name = "total_amount_cents")
    val totalAmountCents: Long,
)

data class CategoryTransactionRankingRow(
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    val notes: String,
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    @ColumnInfo(name = "created_on")
    val createdOn: java.util.Date,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "custom_image_path")
    val customImagePath: String?,
    @ColumnInfo(name = "default_category_key")
    val defaultCategoryKey: String?,
)
