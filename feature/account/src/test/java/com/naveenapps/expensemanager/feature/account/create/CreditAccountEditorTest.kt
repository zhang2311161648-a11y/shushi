package com.naveenapps.expensemanager.feature.account.create

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.AccountType
import org.junit.Test

class CreditAccountEditorTest {

    @Test
    fun creditEditorStoresPositiveDebtAsNegativeLedgerBalance() {
        assertThat(
            AccountCreateViewModel.storedBalanceAmount(
                accountType = AccountType.CREDIT,
                enteredAmount = 234.50,
            ),
        ).isEqualTo(-234.50)
    }

    @Test
    fun regularEditorKeepsPositiveBalanceAsPositiveLedgerBalance() {
        assertThat(
            AccountCreateViewModel.storedBalanceAmount(
                accountType = AccountType.REGULAR,
                enteredAmount = 234.50,
            ),
        ).isEqualTo(234.50)
    }
}
