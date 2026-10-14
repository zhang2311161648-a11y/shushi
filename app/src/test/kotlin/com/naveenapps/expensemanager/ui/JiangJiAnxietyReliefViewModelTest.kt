package com.naveenapps.expensemanager.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JiangJiAnxietyReliefViewModelTest {
    @Test
    fun blankTargetUsesTheGlobalDefault() {
        assertEquals(
            500L,
            JiangJiAnxietyReliefViewModel.resolveTargetDailyCostCents("", 500L),
        )
    }

    @Test
    fun invalidNonBlankTargetStillNeedsCorrection() {
        assertNull(
            JiangJiAnxietyReliefViewModel.resolveTargetDailyCostCents("not-a-number", 500L),
        )
    }
}
