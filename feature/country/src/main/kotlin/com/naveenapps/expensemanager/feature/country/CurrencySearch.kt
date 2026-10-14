package com.naveenapps.expensemanager.feature.country

import com.naveenapps.expensemanager.core.model.Country
import java.util.Locale

internal fun Country.matchesSearchQuery(query: String): Boolean {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) return true

    val searchableValues = buildList {
        add(name)
        add(countryCode)
        add(currencyCode)
        add(currency.code)
        add(currency.name)
        add(currency.namePlural)
        add(currency.nativeSymbol)
        add(currency.symbol)
        addAll(chineseCurrencyAliases(currency.code))
    }

    return searchableValues.any { it.contains(normalizedQuery, ignoreCase = true) }
}

private fun chineseCurrencyAliases(code: String): List<String> = when (code.uppercase(Locale.ROOT)) {
    "CNY" -> listOf("人民币", "人民币元", "元", "RMB")
    "HKD" -> listOf("港币", "港元")
    "USD" -> listOf("美元", "美金")
    "JPY" -> listOf("日元", "日币")
    "EUR" -> listOf("欧元")
    "GBP" -> listOf("英镑")
    "KRW" -> listOf("韩元", "韩币")
    "TWD" -> listOf("新台币", "台币")
    "SGD" -> listOf("新加坡元", "新币")
    "AUD" -> listOf("澳元", "澳币")
    "CAD" -> listOf("加元", "加币")
    "CHF" -> listOf("瑞士法郎")
    "INR" -> listOf("印度卢比")
    "RUB" -> listOf("俄罗斯卢布", "卢布")
    "THB" -> listOf("泰铢")
    "MYR" -> listOf("马来西亚林吉特", "马币")
    "MOP" -> listOf("澳门元", "澳门币")
    "NZD" -> listOf("新西兰元")
    "SEK" -> listOf("瑞典克朗")
    "NOK" -> listOf("挪威克朗")
    "DKK" -> listOf("丹麦克朗")
    "ZAR" -> listOf("南非兰特")
    "BRL" -> listOf("巴西雷亚尔")
    "AED" -> listOf("阿联酋迪拉姆")
    "SAR" -> listOf("沙特里亚尔")
    else -> emptyList()
}
