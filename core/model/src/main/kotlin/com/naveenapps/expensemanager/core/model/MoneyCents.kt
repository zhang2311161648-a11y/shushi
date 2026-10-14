package com.naveenapps.expensemanager.core.model

import java.math.BigDecimal
import java.math.RoundingMode

/** Converts the legacy display amount to the exact cents representation used by balances. */
fun Double.toCents(): Long = BigDecimal.valueOf(this)
    .movePointRight(2)
    .setScale(0, RoundingMode.HALF_UP)
    .longValueExact()

/** Converts a cents balance to the legacy display amount at the repository/UI boundary. */
fun Long.toAmountDouble(): Double = this.toDouble() / 100.0
