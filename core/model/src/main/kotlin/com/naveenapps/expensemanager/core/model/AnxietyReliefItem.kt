package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

/**
 * A private, standalone wellbeing helper. It is deliberately not a transaction or account
 * model, so it cannot be included in income/expense aggregates by accident.
 */
@Stable
data class AnxietyReliefItem(
    val id: String,
    val name: String,
    val imageUri: String?,
    val priceCents: Long,
    val purchaseDate: String,
    val targetDailyCostCents: Long = 100L,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false,
    val mood: String = "开心 🥰",
    val category: String = "未分类",
    val isSold: Boolean = false,
    val costByUse: Boolean = false,
    val useCount: Long = 0,
)

data class AnxietyReliefMetrics(
    val ownedDays: Long,
    val avgDailyCostCents: Long,
    val targetTotalDays: Long,
    val remainingDays: Long,
    val isPaidBack: Boolean,
)

object AnxietyReliefCalculator {
    fun calculate(item: AnxietyReliefItem, today: LocalDate): AnxietyReliefMetrics {
        require(item.priceCents >= 0L) { "priceCents must not be negative" }
        require(item.targetDailyCostCents > 0L) { "targetDailyCostCents must be positive" }

        val purchaseDate = LocalDate.parse(item.purchaseDate)
        val ownedDays = maxOf(1L, today.toEpochDays() - purchaseDate.toEpochDays() + 1L)
        val avgDailyCostCents = item.priceCents / ownedDays
        val targetTotalDays = ceilDivide(item.priceCents, item.targetDailyCostCents)
        val remainingDays = maxOf(0L, targetTotalDays - ownedDays)

        // Compare the exact fractions without Float/Double rounding.
        val isPaidBack = targetTotalDays <= ownedDays

        return AnxietyReliefMetrics(
            ownedDays = ownedDays,
            avgDailyCostCents = avgDailyCostCents,
            targetTotalDays = targetTotalDays,
            remainingDays = remainingDays,
            isPaidBack = isPaidBack,
        )
    }

    private fun ceilDivide(value: Long, divisor: Long): Long {
        if (value == 0L) return 0L
        return value / divisor + if (value % divisor == 0L) 0L else 1L
    }
}
