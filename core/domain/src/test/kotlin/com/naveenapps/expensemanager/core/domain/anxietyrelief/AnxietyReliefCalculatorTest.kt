package com.naveenapps.expensemanager.core.domain.anxietyrelief

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AnxietyReliefCalculator
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import kotlinx.datetime.LocalDate
import org.junit.Test

class AnxietyReliefCalculatorTest {

    @Test
    fun `owned days includes purchase day and never falls below one`() {
        val item = item(purchaseDate = "2026-08-30")

        assertThat(AnxietyReliefCalculator.calculate(item, LocalDate(2026, 8, 30)).ownedDays)
            .isEqualTo(1L)
        assertThat(AnxietyReliefCalculator.calculate(item, LocalDate(2026, 8, 29)).ownedDays)
            .isEqualTo(1L)
        assertThat(AnxietyReliefCalculator.calculate(item, LocalDate(2026, 9, 2)).ownedDays)
            .isEqualTo(4L)
    }

    @Test
    fun `average daily cost uses integer cents without floating point`() {
        val metrics = AnxietyReliefCalculator.calculate(
            item(priceCents = 1_000L, purchaseDate = "2026-08-28"),
            LocalDate(2026, 8, 30),
        )

        assertThat(metrics.avgDailyCostCents).isEqualTo(333L)
    }

    @Test
    fun `remaining days uses ceiling target duration`() {
        val metrics = AnxietyReliefCalculator.calculate(
            item(priceCents = 1_001L, targetDailyCostCents = 500L, purchaseDate = "2026-08-30"),
            LocalDate(2026, 8, 30),
        )

        assertThat(metrics.targetTotalDays).isEqualTo(3L)
        assertThat(metrics.remainingDays).isEqualTo(2L)
    }

    @Test
    fun `paid back is true when exact daily cost reaches target`() {
        val metrics = AnxietyReliefCalculator.calculate(
            item(priceCents = 1_000L, targetDailyCostCents = 500L, purchaseDate = "2026-08-29"),
            LocalDate(2026, 8, 30),
        )

        assertThat(metrics.avgDailyCostCents).isEqualTo(500L)
        assertThat(metrics.remainingDays).isEqualTo(0L)
        assertThat(metrics.isPaidBack).isTrue()
    }

    @Test
    fun `anxiety relief item is calculated independently from transaction statistics`() {
        val item = item(priceCents = 9_900L, purchaseDate = "2026-08-01")
        val before = AnxietyReliefCalculator.calculate(item, LocalDate(2026, 8, 30))
        val after = AnxietyReliefCalculator.calculate(item.copy(note = "a transaction-like note"), LocalDate(2026, 8, 30))

        // The standalone calculator has no transaction/account/budget input or side effect.
        assertThat(after).isEqualTo(before)
    }

    private fun item(
        priceCents: Long = 5_000L,
        purchaseDate: String = "2026-08-01",
        targetDailyCostCents: Long = 500L,
    ) = AnxietyReliefItem(
        id = "item-1",
        name = "测试物品",
        imageUri = null,
        priceCents = priceCents,
        purchaseDate = purchaseDate,
        targetDailyCostCents = targetDailyCostCents,
        note = "",
        createdAt = 0L,
        updatedAt = 0L,
    )
}
