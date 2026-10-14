package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country
import com.naveenapps.expensemanager.core.model.Currency
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencySearchTest {

    private val chineseYuan = Country(
        name = "China",
        countryCode = "CN",
        currencyCode = "CNY",
        currency = Currency(
            name = "Chinese Yuan",
            symbol = "¥",
            nativeSymbol = "元",
            code = "CNY",
        ),
    )

    @Test
    fun searchesChineseCurrencyName() {
        assertTrue(chineseYuan.matchesSearchQuery("人民币"))
    }

    @Test
    fun searchesCurrencyCodeAndSymbol() {
        assertTrue(chineseYuan.matchesSearchQuery("CNY"))
        assertTrue(chineseYuan.matchesSearchQuery("¥"))
    }

    @Test
    fun doesNotMatchUnrelatedCurrencyQuery() {
        assertFalse(chineseYuan.matchesSearchQuery("港币"))
    }
}
