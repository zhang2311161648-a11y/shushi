package com.naveenapps.expensemanager.core.model

import org.junit.Assert.*
import org.junit.Test

class AccountBalanceOverflowTest {
    @Test fun arithmeticOverflowRejectsInsteadOfWrappingBalance() {
        assertTrue(runCatching { calculateAccountBalances(mapOf("wallet" to Long.MAX_VALUE),listOf(BalanceCalculationTransaction("x",TransactionType.INCOME,1,"wallet",null,1))) }.exceptionOrNull() is ArithmeticException)
        assertTrue(runCatching { calculateAccountBalances(mapOf("wallet" to Long.MIN_VALUE),listOf(BalanceCalculationTransaction("x",TransactionType.EXPENSE,1,"wallet",null,1))) }.exceptionOrNull() is ArithmeticException)
    }
}
