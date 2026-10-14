package com.naveenapps.expensemanager.core.model

import java.util.UUID

enum class SavingsPlanType(val title: String, val badge: String, val description: String) {
    FIXED("定额存钱法", "定额存钱", "每月固定金额，持续不变，可按天/周/月周期存入"),
    FLEXIBLE("灵活存钱法", "灵活存钱", "根据个人情况自由设定金额、时间和周期"),
    WEEK_52("52周存钱法", "52周", "一年52周，每周递增存入，积少成多"),
    DAY_365("365天存钱法", "365天", "365天每天存入相应金额，打卡式存钱"),
    ;

    companion object {
        fun fromName(name: String?): SavingsPlanType =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FIXED
    }
}

data class SavingsCheckIn(
    val id: String = UUID.randomUUID().toString(),
    val date: String,
    val amountCents: Long,
    val createdAt: Long = System.currentTimeMillis(),
)

data class SavingsPlan(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: SavingsPlanType,
    val targetAmountCents: Long,
    val savedAmountCents: Long = 0L,
    val startDate: String,
    val endDate: String,
    val frequency: String = "每周",
    val note: String = "",
    val checkIns: List<SavingsCheckIn> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

fun SavingsPlan.nextSuggestedAmountCents(): Long? = when (type) {
    SavingsPlanType.WEEK_52 -> if (checkIns.size < 52) (checkIns.size + 1L) * 1000L else null
    SavingsPlanType.DAY_365 -> if (checkIns.size < 365) (checkIns.size + 1L) * 100L else null
    else -> null
}

fun parseSavingsAmountCents(text: String): Long? = runCatching {
    java.math.BigDecimal(text.trim()).setScale(2, java.math.RoundingMode.UNNECESSARY)
        .movePointRight(2).longValueExact().takeIf { it > 0L }
}.getOrNull()
