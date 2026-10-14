package com.naveenapps.expensemanager.core.model

enum class AccountType {
    REGULAR,
    CREDIT,
    // Appended to preserve the ordinal values already stored for REGULAR/CREDIT.
    BANK_CARD,
    CASH,
    WECHAT,
    ALIPAY,
    OTHER,
}

fun AccountType.isRegular() = this == AccountType.REGULAR

fun AccountType.isCredit() = this == AccountType.CREDIT
