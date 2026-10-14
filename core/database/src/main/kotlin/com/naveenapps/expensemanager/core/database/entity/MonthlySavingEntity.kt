package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_saving",
    indices = [Index(value = ["period_key"], unique = true)],
)
data class MonthlySavingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val year: Int,
    val month: Int,
    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
    val note: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "period_key", defaultValue = "NULL")
    val periodKey: String? = null,
    @ColumnInfo(name = "period_start_date", defaultValue = "NULL")
    val periodStartDate: String? = null,
    @ColumnInfo(name = "period_end_date", defaultValue = "NULL")
    val periodEndExclusiveDate: String? = null,
)
