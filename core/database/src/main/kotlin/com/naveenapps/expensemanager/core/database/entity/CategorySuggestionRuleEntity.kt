package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_suggestion_rule",
    indices = [
        Index(value = ["normalized_pattern", "category_id", "match_type", "source"], unique = true),
        Index(value = ["category_id"]),
        Index(value = ["ledger_id"]),
    ],
)
data class CategorySuggestionRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "pattern")
    val pattern: String,
    @ColumnInfo(name = "normalized_pattern")
    val normalizedPattern: String,
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "match_type")
    val matchType: Int,
    @ColumnInfo(name = "source")
    val source: Int,
    @ColumnInfo(name = "usage_count")
    val usageCount: Int,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long,
    @ColumnInfo(name = "ledger_id", defaultValue = "'default'")
    val ledgerId: String = com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID,
)
