package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

/** The id used by the migration for all data that existed before ledgers were introduced. */
const val DEFAULT_LEDGER_ID = "default"

@Stable
data class Ledger(
    val id: String,
    val name: String,
    val createdOn: Date,
    val updatedOn: Date,
    val isArchived: Boolean = false,
)
