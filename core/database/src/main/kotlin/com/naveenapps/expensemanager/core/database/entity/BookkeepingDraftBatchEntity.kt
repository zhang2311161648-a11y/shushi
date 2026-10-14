package com.naveenapps.expensemanager.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "bookkeeping_draft_batch", indices = [Index(value = ["ledgerId"])])
data class BookkeepingDraftBatchEntity(
    @PrimaryKey val id: String,
    val ledgerId: String,
    val payload: String,
    val state: String,
    val createdAt: Long,
    val updatedAt: Long,
)
