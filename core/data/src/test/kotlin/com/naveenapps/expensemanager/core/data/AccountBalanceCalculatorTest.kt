package com.naveenapps.expensemanager.core.data

import com.naveenapps.expensemanager.core.model.BalanceCalculationTransaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.calculateAccountBalances
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountBalanceCalculatorTest {

    @Test
    fun replaysExpenseLedgerAndStoresPostTransactionSnapshots() {
        val result = calculateAccountBalances(
            initialBalances = mapOf("bank" to 10_000L),
            transactions = listOf(
                transaction("first", TransactionType.EXPENSE, 2_000L, 1L),
                transaction("second", TransactionType.EXPENSE, 3_000L, 2L),
            ),
        )

        assertEquals(5_000L, result.currentBalances["bank"])
        assertEquals(8_000L, result.balanceAfterTransactions["first"])
        assertEquals(5_000L, result.balanceAfterTransactions["second"])
    }

    @Test
    fun incomeAndTransferUpdateTheCorrectAccounts() {
        val result = calculateAccountBalances(
            initialBalances = mapOf("bank" to 10_000L, "wallet" to 1_000L),
            transactions = listOf(
                transaction("income", TransactionType.INCOME, 5_000L, 1L),
                transaction("transfer", TransactionType.TRANSFER, 2_000L, 2L, "wallet"),
            ),
        )

        assertEquals(13_000L, result.currentBalances["bank"])
        assertEquals(3_000L, result.currentBalances["wallet"])
        assertEquals(15_000L, result.balanceAfterTransactions["income"])
        assertEquals(13_000L, result.balanceAfterTransactions["transfer"])
    }

    @Test
    fun editingOrDeletingIsRepresentedByReplayingTheNewLedger() {
        val edited = calculateAccountBalances(
            initialBalances = mapOf("bank" to 10_000L),
            transactions = listOf(
                transaction("first", TransactionType.EXPENSE, 4_000L, 1L),
            ),
        )
        val deleted = calculateAccountBalances(
            initialBalances = mapOf("bank" to 10_000L),
            transactions = emptyList(),
        )

        assertEquals(6_000L, edited.currentBalances["bank"])
        assertEquals(10_000L, deleted.currentBalances["bank"])
    }

    private fun transaction(
        id: String,
        type: TransactionType,
        amountCents: Long,
        createdAtMillis: Long,
        toAccountId: String? = null,
    ) = BalanceCalculationTransaction(
        id = id,
        type = type,
        amountCents = amountCents,
        fromAccountId = "bank",
        toAccountId = toAccountId,
        createdAtMillis = createdAtMillis,
    )
}
