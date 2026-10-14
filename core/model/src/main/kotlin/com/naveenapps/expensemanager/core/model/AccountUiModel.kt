package com.naveenapps.expensemanager.core.model

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.common.R

@Stable
data class AccountUiModel(
    val id: String,
    val name: String,
    val storedIcon: StoredIcon,
    val amount: Amount,
    val amountTextColor: Int,
    val type: AccountType = AccountType.REGULAR,
    val availableCreditLimit: Amount? = null,
)

fun Account.toAccountUiModel(
    amount: Amount,
    availableCreditLimit: Amount? = null
) = AccountUiModel(
    id = this.id,
    name = this.name,
    storedIcon = this.storedIcon,
    amount = amount,
    type = this.type,
    // Account balance is not income. Keep green reserved for actual income amounts.
    amountTextColor = R.color.burgundy_700,
    availableCreditLimit = availableCreditLimit
)
