package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salary_cycle",
    indices = [Index(value = ["start_date"], unique = true)],
)
data class SalaryCycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "start_date")
    val startDate: String,
    val note: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
