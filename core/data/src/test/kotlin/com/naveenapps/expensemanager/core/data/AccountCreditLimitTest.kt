package com.naveenapps.expensemanager.core.data

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.getAvailableCreditLimitCents
import com.naveenapps.expensemanager.core.model.getCreditOwedCents
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountCreditLimitTest {

    @Test
    fun availableCreditIsTotalLimitMinusCurrentDebt() {
        val account = creditAccount(
            totalLimitCents = 100_000L,
            currentBalanceCents = -23_450L,
        )

        assertEquals(23_450L, account.getCreditOwedCents())
        assertEquals(76_550L, account.getAvailableCreditLimitCents())
    }

    @Test
    fun creditRefundDoesNotTurnIntoNegativeDebt() {
        val account = creditAccount(
            totalLimitCents = 100_000L,
            currentBalanceCents = 2_000L,
        )

        assertEquals(0L, account.getCreditOwedCents())
        assertEquals(100_000L, account.getAvailableCreditLimitCents())
    }

    private fun creditAccount(
        totalLimitCents: Long,
        currentBalanceCents: Long,
    ) = Account(
        id = "credit",
        name = "花呗",
        type = AccountType.CREDIT,
        storedIcon = StoredIcon(name = "credit_card", backgroundColor = "#8B1E3F"),
        createdOn = Date(0L),
        updatedOn = Date(0L),
        creditLimit = totalLimitCents / 100.0,
        amount = currentBalanceCents / 100.0,
        initialBalanceCents = currentBalanceCents,
        currentBalanceCents = currentBalanceCents,
    )
}
