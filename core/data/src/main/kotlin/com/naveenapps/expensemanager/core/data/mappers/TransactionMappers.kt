package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionCurrencySnapshot
import com.naveenapps.expensemanager.core.model.toAmountDouble
import com.naveenapps.expensemanager.core.model.toCents

private val legacyCurrency = Currency(
    symbol = "¥",
    name = "Chinese Yuan",
    position = CurrencyPosition.PREFIX,
    code = "CNY",
)

private fun currency(
    code: String,
    symbol: String,
    name: String,
    position: Int,
): Currency = Currency(
    symbol = symbol,
    name = name,
    position = CurrencyPosition.entries.getOrElse(position) { CurrencyPosition.PREFIX },
    code = code,
)

private fun Currency.asLegacyNullable(): Currency? =
    takeUnless { it.code.equals("CNY", ignoreCase = true) && it.symbol == "¥" }

fun Transaction.toEntityModel(): TransactionEntity {
    val snapshot = currencySnapshot ?: TransactionCurrencySnapshot(
        originalAmountCents = amount.amount.toCents(),
        originalCurrency = amount.currency ?: legacyCurrency,
        baseAmountCents = amount.amount.toCents(),
        baseCurrency = legacyCurrency,
        exchangeRateMicros = 1_000_000L,
        exchangeRateSource = "legacy",
        exchangeRateUpdatedAt = null,
    )
    return TransactionEntity(
        id = id,
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = snapshot.originalAmountCents.toAmountDouble(),
        imagePath = imagePath,
        createdOn = createdOn,
        updatedOn = updatedOn,
        balanceAfterTransactionCents = balanceAfterTransactionCents,
        originalAmountCents = snapshot.originalAmountCents,
        originalCurrencyCode = snapshot.originalCurrency.code,
        originalCurrencySymbol = snapshot.originalCurrency.symbol,
        originalCurrencyName = snapshot.originalCurrency.name,
        originalCurrencyPosition = snapshot.originalCurrency.position.ordinal,
        baseAmountCents = snapshot.baseAmountCents,
        baseCurrencyCode = snapshot.baseCurrency.code,
        baseCurrencySymbol = snapshot.baseCurrency.symbol,
        baseCurrencyName = snapshot.baseCurrency.name,
        baseCurrencyPosition = snapshot.baseCurrency.position.ordinal,
        exchangeRateMicros = snapshot.exchangeRateMicros,
        exchangeRateSource = snapshot.exchangeRateSource,
        exchangeRateUpdatedAt = snapshot.exchangeRateUpdatedAt,
    )
}

fun TransactionEntity.toDomainModel(): Transaction {
    return Transaction(
        id = id,
        notes = notes,
        categoryId = categoryId,
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        type = type,
        amount = Amount(
            amount = originalAmountCents.toAmountDouble(),
            currency = currency(
                code = originalCurrencyCode,
                symbol = originalCurrencySymbol,
                name = originalCurrencyName,
                position = originalCurrencyPosition,
            ).asLegacyNullable(),
        ),
        imagePath = imagePath,
        createdOn = createdOn,
        updatedOn = updatedOn,
        balanceAfterTransactionCents = balanceAfterTransactionCents,
        currencySnapshot = TransactionCurrencySnapshot(
            originalAmountCents = originalAmountCents,
            originalCurrency = currency(
                code = originalCurrencyCode,
                symbol = originalCurrencySymbol,
                name = originalCurrencyName,
                position = originalCurrencyPosition,
            ),
            baseAmountCents = baseAmountCents,
            baseCurrency = currency(
                code = baseCurrencyCode,
                symbol = baseCurrencySymbol,
                name = baseCurrencyName,
                position = baseCurrencyPosition,
            ),
            exchangeRateMicros = exchangeRateMicros,
            exchangeRateSource = exchangeRateSource,
            exchangeRateUpdatedAt = exchangeRateUpdatedAt,
        ),
    )
}
