package com.naveenapps.expensemanager.ui

import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Test

class AnxietyAssetOverviewTest {
    private val today = LocalDate.parse("2026-10-12")
    private val item = AnxietyReliefItem("camera", "相机", null, 1000, "2026-10-11", 300, "", 1, 1, costByUse = true)

    @Test fun unusedItemHasNoInventedPerUseCost() {
        assertNull(assetAverage(item, today))
        assertEquals(0f, assetProgress(item, today), 0f)
    }
    @Test fun useTargetRequiresEnoughWholeUses() {
        assertEquals(333L, assetAverage(item.copy(useCount = 3), today))
        assertEquals(.75f, assetProgress(item.copy(useCount = 3), today), 0f)
        assertEquals(1f, assetProgress(item.copy(useCount = 4), today), 0f)
    }
    @Test fun dailyCostUsesInclusiveOwnershipDays() {
        assertEquals(500L, assetAverage(item.copy(costByUse = false), today))
        assertEquals(.5f, assetProgress(item.copy(costByUse = false), today), 0f)
    }
}
