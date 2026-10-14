package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.time.LocalDate

@Stable
data class MonthlySaving(
    val id: Long = 0L,
    val year: Int,
    val month: Int,
    val amountCents: Long,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    /** Stable identity for the accounting period this saving belongs to. */
    val periodKey: String? = null,
    val periodStartDate: LocalDate? = null,
    val periodEndExclusiveDate: LocalDate? = null,
)

/** Keeps savings for different accounting modes distinct even when they share a calendar month. */
fun AccountingPeriod.toMonthlySavingPeriodKey(): String = "${mode.name}:$startDate"
