package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anxiety_relief_item")
data class AnxietyReliefItemEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "image_uri")
    val imageUri: String?,
    @ColumnInfo(name = "price_cents")
    val priceCents: Long,
    @ColumnInfo(name = "purchase_date")
    val purchaseDate: String,
    @ColumnInfo(name = "target_daily_cost_cents")
    val targetDailyCostCents: Long,
    @ColumnInfo(name = "note")
    val note: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean,
    @ColumnInfo(name = "category", defaultValue = "'未分类'")
    val category: String = "未分类",
    @ColumnInfo(name = "is_sold", defaultValue = "0")
    val isSold: Boolean = false,
    @ColumnInfo(name = "cost_by_use", defaultValue = "0")
    val costByUse: Boolean = false,
    @ColumnInfo(name = "use_count", defaultValue = "0")
    val useCount: Long = 0,
)
