package com.naveenapps.expensemanager.core.data.importer

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class CsvImportParserTest {

    private val parser = CsvImportParser()

    @Test
    fun parsesCurrentEnglishExportAfterSummaryRows() {
        val result = parser.parse(
            listOf(
                arrayOf("Exported On", "August 30, 2026 15:00"),
                arrayOf("Total Transactions", "1"),
                arrayOf("Total Income", "0.00"),
                arrayOf("Total Expense", "55.00"),
                arrayOf("Net Balance", "-55.00"),
                emptyArray(),
                arrayOf(
                    "Date", "Time", "Transaction Type", "Category", "Category Type",
                    "From Account", "Account Type", "To Account", "Amount", "Notes",
                ),
                arrayOf(
                    "August 23, 2026", "14:30", "Expense", "Food", "Expense", "Cash",
                    "Regular", "", "-55.00", "Bubble tea",
                ),
            ),
        )

        assertEquals(1, result.transactions.size)
        assertEquals(0, result.skippedCount)
        val transaction = result.transactions.single()
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(CategoryType.EXPENSE, transaction.categoryType)
        assertEquals(5_500L, transaction.amountCents)
        assertEquals("Bubble tea", transaction.notes)
        assertEquals(2026, transaction.date.year())
        assertEquals(Calendar.AUGUST, transaction.date.month())
        assertEquals(23, transaction.date.day())
    }

    @Test
    fun parsesChineseHeadersAndDateWithoutDependingOnCurrentLocale() {
        val result = parser.parse(
            listOf(
                arrayOf("导出时间", "2026年08月30日 15:00"),
                arrayOf("交易总数", "1"),
                arrayOf("总收入", "0"),
                arrayOf("总支出", "55"),
                arrayOf("净余额", "-55"),
                emptyArray(),
                arrayOf(
                    "日期", "时间", "交易类型", "分类", "分类类型", "转出账户", "账户类型",
                    "转入账户", "金额", "备注",
                ),
                arrayOf(
                    "2026年08月23日", "14:30", "支出", "餐饮", "支出", "现金", "普通", "", "¥55.00", "食材",
                ),
            ),
        )

        assertEquals(1, result.transactions.size)
        val transaction = result.transactions.single()
        assertEquals(TransactionType.EXPENSE, transaction.type)
        assertEquals(5_500L, transaction.amountCents)
        assertEquals("现金", transaction.fromAccountName)
        assertEquals("餐饮", transaction.categoryName)
    }

    @Test
    fun parsesCurrencySnapshotColumnsFromNewExport() {
        val result = parser.parse(
            listOf(
                arrayOf(
                    "Date", "Time", "Transaction Type", "Category", "Category Type",
                    "From Account", "Account Type", "To Account", "Amount", "Notes",
                    "Original Amount", "Original Currency Code", "Original Currency Symbol",
                    "Original Currency Name", "Base Amount", "Base Currency Code",
                    "Base Currency Symbol", "Base Currency Name", "Exchange Rate Micros",
                    "Exchange Rate Source",
                ),
                arrayOf(
                    "2026-08-23", "14:30", "Expense", "Food", "Expense", "Cash", "Regular", "",
                    "-55.00", "Bubble tea", "-55.00", "HKD", "HK$", "Hong Kong Dollar",
                    "-50.89", "CNY", "¥", "Chinese Yuan", "925000", "manual",
                ),
            ),
        )

        val transaction = result.transactions.single()
        assertEquals(5_500L, transaction.originalAmountCents)
        assertEquals("HKD", transaction.originalCurrencyCode)
        assertEquals(5_089L, transaction.baseAmountCents)
        assertEquals("CNY", transaction.baseCurrencyCode)
        assertEquals(925_000L, transaction.exchangeRateMicros)
        assertEquals("manual", transaction.exchangeRateSource)
    }

    @Test
    fun parsesLegacyRowsWithoutRecognizedHeaderAndEuropeanAmount() {
        val result = parser.parse(
            listOf(
                arrayOf(
                    "23.08.2026", "09:10", "Income", "Salary", "Income", "Bank Account",
                    "Regular", "", "1.234,50", "August salary",
                ),
            ),
        )

        assertEquals(false, result.hasTransactionHeader)
        assertEquals(1, result.transactions.size)
        val transaction = result.transactions.single()
        assertEquals(TransactionType.INCOME, transaction.type)
        assertEquals(CategoryType.INCOME, transaction.categoryType)
        assertEquals(123_450L, transaction.amountCents)
        assertNotNull(transaction.date)
    }

    @Test
    fun skipsMalformedDataButNotExportSummaryRows() {
        val result = parser.parse(
            listOf(
                arrayOf("Exported On", "August 30, 2026"),
                arrayOf("Date", "Time", "Type", "Category", "Amount"),
                arrayOf("not-a-date", "12:00", "Expense", "Food", "12.00"),
                arrayOf("August 23, 2026", "12:00", "Expense", "Food", "12.00"),
            ),
        )

        assertEquals(1, result.transactions.size)
        assertEquals(1, result.skippedCount)
    }

    private fun java.util.Date.year(): Int {
        return Calendar.getInstance().apply { time = this@year }.get(Calendar.YEAR)
    }

    private fun java.util.Date.month(): Int {
        return Calendar.getInstance().apply { time = this@month }.get(Calendar.MONTH)
    }

    private fun java.util.Date.day(): Int {
        return Calendar.getInstance().apply { time = this@day }.get(Calendar.DAY_OF_MONTH)
    }
}
