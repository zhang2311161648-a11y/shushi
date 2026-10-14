package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import java.util.Date

@Stable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val storedIcon: StoredIcon,
    val createdOn: Date,
    val updatedOn: Date,
    val sequence: Int = Int.MAX_VALUE,
    val amount: Double = 0.0,
    val creditLimit: Double = 0.0,
    val initialBalanceCents: Long = amount.toCents(),
    val currentBalanceCents: Long = amount.toCents(),
)

fun Account.getAvailableCreditLimit(): Double {
    return getAvailableCreditLimitCents().toAmountDouble()
}

/**
 * The signed ledger balance of a credit account is negative when money is owed.
 * Expose the user-facing debt as a positive cents value so the editor can say
 * "current debt" without changing the balance replay model.
 */
fun Account.getCreditOwedCents(): Long {
    return if (type.isCredit()) {
        (-currentBalanceCents).coerceAtLeast(0L)
    } else {
        0L
    }
}

/**
 * Available credit is the total limit minus the current debt. Keep this in cents
 * so account lists and transaction selection do not reintroduce floating-point
 * rounding at the presentation boundary.
 */
fun Account.getAvailableCreditLimitCents(): Long {
    return creditLimit.toCents() - getCreditOwedCents()
}
