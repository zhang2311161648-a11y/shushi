package com.naveenapps.expensemanager.core.model

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/** Number of fixed-point units used to store an exchange rate. */
const val EXCHANGE_RATE_SCALE: Long = 1_000_000L

/** Immutable original/base currency and exchange-rate snapshot for one transaction. */
data class TransactionCurrencySnapshot(
    val originalAmountCents: Long,
    val originalCurrency: Currency,
    val baseAmountCents: Long,
    val baseCurrency: Currency,
    val exchangeRateMicros: Long,
    val exchangeRateSource: String,
    val exchangeRateUpdatedAt: Long?,
)

data class ExchangeRateSnapshot(
    val fromCurrencyCode: String,
    val toCurrencyCode: String,
    val rateMicros: Long,
    val source: String,
    val updatedAtMillis: Long,
)

/** Converts a decimal rate such as 7.8125 to fixed-point millionths. */
fun String.toExchangeRateMicros(): Long = BigDecimal(trim())
    .movePointRight(6)
    .setScale(0, RoundingMode.HALF_UP)
    .longValueExact()
    .also { require(it > 0) { "Exchange rate must be greater than zero" } }

/** Converts cents with integer arithmetic and half-up rounding at the cent boundary. */
fun convertCents(amountCents: Long, rateMicros: Long): Long {
    require(amountCents >= 0) { "Amount cents must not be negative" }
    require(rateMicros > 0) { "Exchange rate must be greater than zero" }
    return BigInteger.valueOf(amountCents)
        .multiply(BigInteger.valueOf(rateMicros))
        .add(BigInteger.valueOf(EXCHANGE_RATE_SCALE / 2))
        .divide(BigInteger.valueOf(EXCHANGE_RATE_SCALE))
        .longValueExact()
}

fun Transaction.effectiveOriginalAmountCents(): Long =
    currencySnapshot?.originalAmountCents ?: amount.amount.toCents()

fun Transaction.effectiveBaseAmountCents(): Long =
    currencySnapshot?.baseAmountCents ?: amount.amount.toCents()

fun Transaction.effectiveOriginalCurrency(): Currency? =
    currencySnapshot?.originalCurrency ?: amount.currency

