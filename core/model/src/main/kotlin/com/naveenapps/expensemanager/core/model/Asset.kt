package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

/** A standalone asset balance that is intentionally separate from transactions. */
@Stable
data class Asset(
    val id: String,
    val name: String,
    val amount: Double,
    val note: String,
    val createdOn: Date,
    val updatedOn: Date,
)
