package com.naveenapps.expensemanager.core.data

import com.naveenapps.expensemanager.core.model.convertCents
import com.naveenapps.expensemanager.core.model.toExchangeRateMicros
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionCurrencyTest {

    @Test
    fun convertsCentsWithFixedPrecisionAndHalfUpRounding() {
        assertEquals(7_812_500L, "7.8125".toExchangeRateMicros())
        assertEquals(7_813L, convertCents(1_000L, 7_812_500L))
    }

    @Test
    fun convertsOneToOneWithoutFloatingPointDrift() {
        assertEquals(12_345L, convertCents(12_345L, 1_000_000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNonPositiveRate() {
        convertCents(100L, 0L)
    }
}
