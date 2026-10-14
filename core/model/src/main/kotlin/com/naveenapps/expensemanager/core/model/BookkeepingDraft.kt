package com.naveenapps.expensemanager.core.model

import java.time.LocalDate

data class BookkeepingDraft(
    val id: String,
    val amountCents: Long?,
    val date: LocalDate?,
    val direction: TransactionType?,
    val note: String,
    val source: String,
    val evidence: String,
    val accountId: String? = null,
    val toAccountId: String? = null,
    val categoryId: String? = null,
    val merchant: String = "",
    val issues: List<String> = emptyList(),
    val saved: Boolean = false,
    val confirmed: Boolean = false,
    val savedAt: Long? = null,
)
