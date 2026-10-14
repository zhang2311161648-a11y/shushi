package com.naveenapps.expensemanager.core.model

/** Minimal transaction data required to recalculate account balances. */
data class BalanceCalculationTransaction(
    val id: String,
    val type: TransactionType,
    val amountCents: Long,
    val fromAccountId: String,
    val toAccountId: String?,
    val createdAtMillis: Long,
)

data class AccountBalanceCalculation(
    val currentBalances: Map<String, Long>,
    /** The balance after the transaction in its source account. */
    val balanceAfterTransactions: Map<String, Long>,
)

/**
 * Replays a complete account ledger in stable chronological order.
 * Transfers affect both accounts but keep one source-account snapshot, which matches the
 * existing transaction model and the homepage presentation.
 */
fun calculateAccountBalances(
    initialBalances: Map<String, Long>,
    transactions: List<BalanceCalculationTransaction>,
): AccountBalanceCalculation {
    val balances = initialBalances.toMutableMap()
    val snapshots = mutableMapOf<String, Long>()

    transactions
        .sortedWith(compareBy<BalanceCalculationTransaction> { it.createdAtMillis }.thenBy { it.id })
        .forEach { transaction ->
            val amount = transaction.amountCents
            when (transaction.type) {
                TransactionType.INCOME -> {
                    val updated = Math.addExact(balances.getOrDefault(transaction.fromAccountId, 0L), amount)
                    balances[transaction.fromAccountId] = updated
                    snapshots[transaction.id] = updated
                }

                TransactionType.EXPENSE -> {
                    val updated = Math.subtractExact(balances.getOrDefault(transaction.fromAccountId, 0L), amount)
                    balances[transaction.fromAccountId] = updated
                    snapshots[transaction.id] = updated
                }

                TransactionType.TRANSFER -> {
                    val sourceUpdated = Math.subtractExact(balances.getOrDefault(transaction.fromAccountId, 0L), amount)
                    balances[transaction.fromAccountId] = sourceUpdated
                    snapshots[transaction.id] = sourceUpdated

                    transaction.toAccountId
                        ?.takeIf { it.isNotBlank() }
                        ?.let { destinationId ->
                            balances[destinationId] =
                                Math.addExact(balances.getOrDefault(destinationId, 0L), amount)
                        }
                }
            }
        }

    return AccountBalanceCalculation(
        currentBalances = balances,
        balanceAfterTransactions = snapshots,
    )
}
