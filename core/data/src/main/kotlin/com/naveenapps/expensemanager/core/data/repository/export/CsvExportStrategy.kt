package com.naveenapps.expensemanager.core.data.repository.export

import android.content.Context
import android.util.Log
import com.naveenapps.expensemanager.core.common.utils.toCapitalize
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.common.utils.toTimeAndMinutes
import com.naveenapps.expensemanager.core.data.R
import com.naveenapps.expensemanager.core.model.ExportData
import com.naveenapps.expensemanager.core.model.ExportFileType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.effectiveBaseAmountCents
import com.naveenapps.expensemanager.core.model.effectiveOriginalAmountCents
import com.naveenapps.expensemanager.core.model.toAmountDouble
import com.opencsv.CSVWriter
import java.util.Date

class CsvExportStrategy(context: Context) : BaseExportStrategy(context) {

    override fun getFileType(): ExportFileType = ExportFileType.CSV

    override fun export(uri: String?, transactions: List<Transaction>): Resource<ExportData> {
        return kotlin.runCatching {
            val fileUri = generateFile(getFileName(), uri) { output ->
                val writer = CSVWriter(output.writer())
                val data = mutableListOf<Array<String>>()

                val totalIncome = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.effectiveBaseAmountCents() }
                val totalExpense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.effectiveBaseAmountCents() }
                val now = Date()

                data.add(
                    arrayOf(
                        context.getString(R.string.exported_on),
                        "${now.toCompleteDate()} ${now.toTimeAndMinutes()}"
                    )
                )
                data.add(
                    arrayOf(
                        context.getString(R.string.total_transactions),
                        transactions.size.toString()
                    )
                )
                data.add(
                    arrayOf(
                        context.getString(R.string.total_income),
                        totalIncome.toAmountDouble().toTrimAmount()
                    )
                )
                data.add(
                    arrayOf(
                        context.getString(R.string.total_expense),
                        totalExpense.toAmountDouble().toTrimAmount()
                    )
                )
                data.add(
                    arrayOf(
                        context.getString(R.string.net_balance),
                        (totalIncome - totalExpense).toAmountDouble().toTrimAmount()
                    )
                )
                data.add(arrayOf())

                data.add(
                    arrayOf(
                        context.getString(R.string.date),
                        context.getString(R.string.time),
                        context.getString(R.string.transaction_type),
                        context.getString(R.string.category),
                        context.getString(R.string.category_type),
                        context.getString(R.string.from_account),
                        context.getString(R.string.account_type),
                        context.getString(R.string.to_account),
                        context.getString(R.string.amount),
                        context.getString(R.string.notes),
                        "Original Amount",
                        "Original Currency Code",
                        "Original Currency Symbol",
                        "Original Currency Name",
                        "Base Amount",
                        "Base Currency Code",
                        "Base Currency Symbol",
                        "Base Currency Name",
                        "Exchange Rate Micros",
                        "Exchange Rate Source",
                    )
                )

                transactions.forEach { transaction ->
                    val signedOriginalAmount = when (transaction.type) {
                        TransactionType.EXPENSE -> -transaction.effectiveOriginalAmountCents()
                        else -> transaction.effectiveOriginalAmountCents()
                    }
                    val signedBaseAmount = when (transaction.type) {
                        TransactionType.EXPENSE -> -transaction.effectiveBaseAmountCents()
                        else -> transaction.effectiveBaseAmountCents()
                    }
                    val snapshot = transaction.currencySnapshot
                    data.add(
                        arrayOf(
                            transaction.createdOn.toCompleteDate(),
                            transaction.createdOn.toTimeAndMinutes(),
                            transaction.type.toCapitalize(),
                            transaction.category.titleResId?.let { context.getString(it) }
                                ?: transaction.category.name,
                            transaction.category.type.toCapitalize(),
                            transaction.fromAccount.name,
                            transaction.fromAccount.type.toCapitalize(),
                            transaction.toAccount?.name ?: "",
                            signedOriginalAmount.toAmountDouble().toTrimAmount(),
                            transaction.notes,
                            signedOriginalAmount.toAmountDouble().toTrimAmount(),
                            snapshot?.originalCurrency?.code ?: transaction.amount.currency?.code.orEmpty().ifBlank { "CNY" },
                            snapshot?.originalCurrency?.symbol ?: transaction.amount.currency?.symbol.orEmpty().ifBlank { "¥" },
                            snapshot?.originalCurrency?.name ?: transaction.amount.currency?.name.orEmpty().ifBlank { "Chinese Yuan" },
                            signedBaseAmount.toAmountDouble().toTrimAmount(),
                            snapshot?.baseCurrency?.code.orEmpty().ifBlank { "CNY" },
                            snapshot?.baseCurrency?.symbol.orEmpty().ifBlank { "¥" },
                            snapshot?.baseCurrency?.name.orEmpty().ifBlank { "Chinese Yuan" },
                            snapshot?.exchangeRateMicros?.toString() ?: "1000000",
                            snapshot?.exchangeRateSource ?: "legacy",
                        )
                    )
                }

                writer.writeAll(data)
                writer.close()
            }
            Resource.Success(fileUri)
        }.onFailure {
            Log.e("CsvExportStrategy", it.localizedMessage ?: "")
        }.getOrNull() ?: Resource.Error(Exception())
    }
}
