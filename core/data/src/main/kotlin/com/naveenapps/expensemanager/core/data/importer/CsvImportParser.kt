package com.naveenapps.expensemanager.core.data.importer

import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ImportedTransaction(
    val date: Date,
    val type: TransactionType,
    val categoryName: String,
    val categoryType: CategoryType?,
    val fromAccountName: String,
    val fromAccountType: AccountType?,
    val toAccountName: String?,
    val toAccountType: AccountType?,
    val amountCents: Long,
    val notes: String,
    val originalAmountCents: Long = amountCents,
    val originalCurrencyCode: String = "CNY",
    val originalCurrencySymbol: String = "¥",
    val originalCurrencyName: String = "Chinese Yuan",
    val baseAmountCents: Long = amountCents,
    val baseCurrencyCode: String = "CNY",
    val baseCurrencySymbol: String = "¥",
    val baseCurrencyName: String = "Chinese Yuan",
    val exchangeRateMicros: Long = 1_000_000L,
    val exchangeRateSource: String = "imported",
)

data class CsvParseResult(
    val transactions: List<ImportedTransaction>,
    val skippedCount: Int,
    val hasTransactionHeader: Boolean,
)

/**
 * Parses the CSV created by [CsvExportStrategy] and the older localized versions of that file.
 * The export contains a few summary rows before the actual transaction header, so parsing is
 * deliberately header-based instead of relying on a fixed row number.
 */
class CsvImportParser {

    fun parse(rows: List<Array<String>>): CsvParseResult {
        val header = rows.withIndex().firstOrNull { (_, row) -> isTransactionHeader(row) }
        val columns = header?.let { TransactionColumns.fromHeader(it.value) }
        val firstDataRow = header?.index?.plus(1) ?: 0
        val transactions = mutableListOf<ImportedTransaction>()
        var skippedCount = 0

        rows.drop(firstDataRow).forEach { row ->
            if (row.all { it.isBlank() } || isSummaryRow(row)) return@forEach

            parseRow(row, columns)
                ?.let(transactions::add)
                ?: run {
                    if (looksLikeTransactionRow(row)) skippedCount++
                }
        }

        return CsvParseResult(
            transactions = transactions,
            skippedCount = skippedCount,
            hasTransactionHeader = header != null,
        )
    }

    private fun parseRow(
        row: Array<String>,
        columns: TransactionColumns?,
    ): ImportedTransaction? {
        val dateText = row.value(columns?.date ?: DATE_INDEX)
        val timeText = row.value(columns?.time ?: TIME_INDEX)
        val categoryText = row.value(columns?.category ?: CATEGORY_INDEX).trim()
        val categoryTypeText = row.value(columns?.categoryType ?: CATEGORY_TYPE_INDEX)
        val typeText = row.value(columns?.type ?: TYPE_INDEX)
        val amountIndex = columns?.amount ?: when {
            row.size >= AMOUNT_INDEX + 1 -> AMOUNT_INDEX
            row.size >= 5 -> row.lastIndex
            else -> AMOUNT_INDEX
        }
        val amountText = row.value(amountIndex)
        val amountCents = parseAmountCents(amountText) ?: return null
        val originalAmountCents = columns?.originalAmount
            ?.let { parseAmountCents(row.value(it)) }
            ?.absoluteValueSafely()
            ?: amountCents.absoluteValueSafely()
        val baseAmountCents = columns?.baseAmount
            ?.let { parseAmountCents(row.value(it)) }
            ?.absoluteValueSafely()
            ?: amountCents.absoluteValueSafely()
        val categoryType = parseCategoryType(categoryTypeText)
        val type = parseTransactionType(typeText)
            ?: categoryType?.let { if (it == CategoryType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE }
            ?: if (amountCents < 0) TransactionType.EXPENSE else TransactionType.INCOME
        val date = parseDate(dateText, timeText) ?: return null

        return ImportedTransaction(
            date = date,
            type = type,
            categoryName = categoryText.ifBlank { UNCATEGORIZED_NAME },
            categoryType = categoryType,
            fromAccountName = row.value(columns?.fromAccount ?: FROM_ACCOUNT_INDEX)
                .trim()
                .ifBlank { IMPORTED_ACCOUNT_NAME },
            fromAccountType = parseAccountType(row.value(columns?.accountType ?: ACCOUNT_TYPE_INDEX)),
            toAccountName = row.value(columns?.toAccount ?: TO_ACCOUNT_INDEX)
                .trim()
                .takeIf { it.isNotEmpty() },
            toAccountType = null,
            amountCents = amountCents.absoluteValueSafely(),
            notes = row.value(columns?.notes ?: NOTES_INDEX).trim(),
            originalAmountCents = originalAmountCents,
            originalCurrencyCode = row.value(columns?.originalCurrencyCode ?: -1).trim().ifBlank { "CNY" },
            originalCurrencySymbol = row.value(columns?.originalCurrencySymbol ?: -1).trim().ifBlank { "¥" },
            originalCurrencyName = row.value(columns?.originalCurrencyName ?: -1).trim().ifBlank { "Chinese Yuan" },
            baseAmountCents = baseAmountCents,
            baseCurrencyCode = row.value(columns?.baseCurrencyCode ?: -1).trim().ifBlank { "CNY" },
            baseCurrencySymbol = row.value(columns?.baseCurrencySymbol ?: -1).trim().ifBlank { "¥" },
            baseCurrencyName = row.value(columns?.baseCurrencyName ?: -1).trim().ifBlank { "Chinese Yuan" },
            exchangeRateMicros = row.value(columns?.exchangeRateMicros ?: -1).trim()
                .toLongOrNull()
                ?.takeIf { it > 0L }
                ?: 1_000_000L,
            exchangeRateSource = row.value(columns?.exchangeRateSource ?: -1).trim().ifBlank { "imported" },
        )
    }

    private fun isTransactionHeader(row: Array<String>): Boolean {
        val fields = row.map(::normalize)
        val date = fields.indexOfFirst { it in DATE_HEADERS }
        val amount = fields.indexOfFirst { it in AMOUNT_HEADERS }
        val type = fields.indexOfFirst { it in TYPE_HEADERS }
        val category = fields.indexOfFirst { it in CATEGORY_HEADERS }
        return date >= 0 && amount >= 0 && (type >= 0 || category >= 0)
    }

    private fun looksLikeTransactionRow(row: Array<String>): Boolean {
        if (row.size < 3) return false
        return row.any { parseTransactionType(it) != null } || row.any { parseAmountCents(it) != null }
    }

    private fun isSummaryRow(row: Array<String>): Boolean {
        val first = normalize(row.firstOrNull().orEmpty())
        return first.isBlank() || first in SUMMARY_HEADERS ||
            first.contains("totaltransactions") || first.contains("totalincome") ||
            first.contains("totalexpense") || first.contains("netbalance") ||
            first.contains("exported") || first.contains("export") ||
            first.contains("导出") || first.contains("总收入") || first.contains("总支出") ||
            first.contains("交易总数") || first.contains("净余额")
    }

    private fun parseTransactionType(value: String): TransactionType? {
        val normalized = normalize(value)
        return when {
            normalized in INCOME_VALUES || normalized.contains("income") ||
                normalized.contains("revenue") || normalized.contains("ingreso") ||
                normalized.contains("receita") || normalized.contains("entrate") ||
                normalized.contains("einnahme") || normalized.contains("доход") ||
                normalized.contains("收入") || normalized.contains("収入") ||
                normalized.contains("수입") || normalized.contains("thunhap") ||
                normalized.contains("pemasukan") || normalized.contains("دخل") -> TransactionType.INCOME

            normalized in TRANSFER_VALUES || normalized.contains("transfer") ||
                normalized.contains("virement") || normalized.contains("trasfer") ||
                normalized.contains("перевод") || normalized.contains("转账") ||
                normalized.contains("振替") || normalized.contains("이체") ||
                normalized.contains("chuyenkhoan") || normalized.contains("havale") -> TransactionType.TRANSFER

            normalized in EXPENSE_VALUES || normalized.contains("expense") ||
                normalized.contains("expenditure") || normalized.contains("depense") ||
                normalized.contains("gasto") || normalized.contains("despesa") ||
                normalized.contains("spesa") || normalized.contains("ausgabe") ||
                normalized.contains("расход") || normalized.contains("支出") ||
                normalized.contains("지출") || normalized.contains("chitieu") ||
                normalized.contains("مصروف") || normalized.contains("gider") ||
                normalized.contains("wydat") || normalized.contains("खर्च") -> TransactionType.EXPENSE

            else -> null
        }
    }

    private fun parseCategoryType(value: String): CategoryType? {
        return when (parseTransactionType(value)) {
            TransactionType.INCOME -> CategoryType.INCOME
            TransactionType.EXPENSE, TransactionType.TRANSFER -> CategoryType.EXPENSE
            null -> null
        }
    }

    private fun parseAccountType(value: String): AccountType? {
        val normalized = normalize(value)
        return when {
            normalized in REGULAR_ACCOUNT_VALUES || normalized.contains("regular") ||
                normalized.contains("standard") || normalized.contains("普通") -> AccountType.REGULAR

            normalized in CREDIT_ACCOUNT_VALUES || normalized.contains("credit") ||
                normalized.contains("信用") -> AccountType.CREDIT

            normalized in BANK_CARD_ACCOUNT_VALUES || normalized.contains("bankcard") ||
                normalized.contains("银行卡") -> AccountType.BANK_CARD

            normalized in CASH_ACCOUNT_VALUES || normalized.contains("cash") ||
                normalized.contains("现金") || normalized.contains("現金") -> AccountType.CASH

            normalized in WECHAT_ACCOUNT_VALUES || normalized.contains("wechat") ||
                normalized.contains("微信") -> AccountType.WECHAT

            normalized in ALIPAY_ACCOUNT_VALUES || normalized.contains("alipay") ||
                normalized.contains("支付宝") -> AccountType.ALIPAY

            normalized in OTHER_ACCOUNT_VALUES || normalized.contains("other") ||
                normalized.contains("其他") -> AccountType.OTHER

            else -> null
        }
    }

    private fun parseAmountCents(value: String): Long? {
        val raw = value.trim()
        if (raw.isEmpty()) return null

        val negative = raw.contains('(') || raw.trimStart().startsWith('-')
        val unsigned = raw
            .replace("(", "")
            .replace(")", "")
            .replace(Regex("[^0-9,.+-]"), "")
            .trimStart('+', '-')
        if (unsigned.isEmpty()) return null

        val normalized = normalizeDecimalSeparators(unsigned)
        return runCatching {
            val cents = BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
            if (negative) -cents else cents
        }.getOrNull()
    }

    private fun normalizeDecimalSeparators(value: String): String {
        val lastComma = value.lastIndexOf(',')
        val lastDot = value.lastIndexOf('.')
        return when {
            lastComma >= 0 && lastDot >= 0 && lastComma > lastDot -> {
                value.replace(".", "").replace(',', '.')
            }

            lastComma >= 0 && lastDot >= 0 -> value.replace(",", "")

            lastComma >= 0 -> normalizeSingleSeparator(value, ',')

            lastDot >= 0 -> normalizeSingleSeparator(value, '.')

            else -> value
        }
    }

    private fun normalizeSingleSeparator(value: String, separator: Char): String {
        val parts = value.split(separator)
        val hasGroupingShape = parts.size > 1 && parts.drop(1).all { it.length == 3 }
        return if (hasGroupingShape) value.replace(separator.toString(), "")
        else value.replace(separator, '.')
    }

    private fun parseDate(dateText: String, timeText: String): Date? {
        val date = dateText.trim()
        val time = timeText.trim()
        if (date.isEmpty()) return null

        val candidates = buildList {
            if (time.isNotEmpty()) add("$date $time")
            add(date)
        }
        val locales = listOf(
            Locale.getDefault(),
            Locale.US,
            Locale.UK,
            Locale.CHINA,
            Locale.JAPAN,
            Locale.KOREA,
            Locale.FRANCE,
            Locale.GERMANY,
            Locale.ITALY,
            Locale("es"),
            Locale("pt"),
            Locale("pl"),
            Locale("ru"),
            Locale("tr"),
            Locale("vi"),
            Locale("ar"),
            Locale("hi"),
        ).distinct()
        val datePatterns = listOf(
            "MMMM dd, yyyy",
            "MMM dd, yyyy",
            "dd MMMM yyyy",
            "dd MMM yyyy",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "MM-dd-yyyy",
            "dd-MM-yyyy",
            "MM.dd.yyyy",
            "dd.MM.yyyy",
            "yyyy年M月d日",
            "yyyy年MM月dd日",
        )
        val timePatterns = listOf("HH:mm", "HH:mm:ss", "hh:mm a")

        for (locale in locales) {
            for (candidate in candidates) {
                val patterns = if (candidate.contains(' ')) {
                    datePatterns.flatMap { datePattern ->
                        timePatterns.map { timePattern -> "$datePattern $timePattern" }
                    }
                } else {
                    datePatterns
                }
                for (pattern in patterns) {
                    parseStrict(candidate, pattern, locale)?.let { return it }
                }
            }
        }
        return null
    }

    private fun parseStrict(value: String, pattern: String, locale: Locale): Date? {
        val format = SimpleDateFormat(pattern, locale).apply { isLenient = false }
        val position = ParsePosition(0)
        val result = format.parse(value, position)
        return result?.takeIf { position.index == value.length }
    }

    private fun Array<String>.value(index: Int): String = getOrNull(index).orEmpty()

    private data class TransactionColumns(
        val date: Int,
        val time: Int?,
        val type: Int?,
        val category: Int?,
        val categoryType: Int?,
        val fromAccount: Int?,
        val accountType: Int?,
        val toAccount: Int?,
        val amount: Int,
        val notes: Int?,
        val originalAmount: Int?,
        val originalCurrencyCode: Int?,
        val originalCurrencySymbol: Int?,
        val originalCurrencyName: Int?,
        val baseAmount: Int?,
        val baseCurrencyCode: Int?,
        val baseCurrencySymbol: Int?,
        val baseCurrencyName: Int?,
        val exchangeRateMicros: Int?,
        val exchangeRateSource: Int?,
    ) {
        companion object {
            fun fromHeader(row: Array<String>) = TransactionColumns(
                date = row.findHeader(DATE_HEADERS) ?: DATE_INDEX,
                time = row.findHeader(TIME_HEADERS),
                type = row.findHeader(TYPE_HEADERS),
                category = row.findHeader(CATEGORY_HEADERS),
                categoryType = row.findHeader(CATEGORY_TYPE_HEADERS),
                fromAccount = row.findHeader(FROM_ACCOUNT_HEADERS),
                accountType = row.findHeader(ACCOUNT_TYPE_HEADERS),
                toAccount = row.findHeader(TO_ACCOUNT_HEADERS),
                amount = row.findHeader(AMOUNT_HEADERS) ?: AMOUNT_INDEX,
                notes = row.findHeader(NOTES_HEADERS),
                originalAmount = row.findHeader(ORIGINAL_AMOUNT_HEADERS),
                originalCurrencyCode = row.findHeader(ORIGINAL_CURRENCY_CODE_HEADERS),
                originalCurrencySymbol = row.findHeader(ORIGINAL_CURRENCY_SYMBOL_HEADERS),
                originalCurrencyName = row.findHeader(ORIGINAL_CURRENCY_NAME_HEADERS),
                baseAmount = row.findHeader(BASE_AMOUNT_HEADERS),
                baseCurrencyCode = row.findHeader(BASE_CURRENCY_CODE_HEADERS),
                baseCurrencySymbol = row.findHeader(BASE_CURRENCY_SYMBOL_HEADERS),
                baseCurrencyName = row.findHeader(BASE_CURRENCY_NAME_HEADERS),
                exchangeRateMicros = row.findHeader(EXCHANGE_RATE_MICROS_HEADERS),
                exchangeRateSource = row.findHeader(EXCHANGE_RATE_SOURCE_HEADERS),
            )

            private fun Array<String>.findHeader(headers: Set<String>): Int? =
                indexOfFirst { normalize(it) in headers }.takeIf { it >= 0 }
        }
    }

    companion object {
        const val IMPORTED_ACCOUNT_NAME = "Imported account"
        const val UNCATEGORIZED_NAME = "Uncategorized"

        private const val DATE_INDEX = 0
        private const val TIME_INDEX = 1
        private const val TYPE_INDEX = 2
        private const val CATEGORY_INDEX = 3
        private const val CATEGORY_TYPE_INDEX = 4
        private const val FROM_ACCOUNT_INDEX = 5
        private const val ACCOUNT_TYPE_INDEX = 6
        private const val TO_ACCOUNT_INDEX = 7
        private const val AMOUNT_INDEX = 8
        private const val NOTES_INDEX = 9

        private val DATE_HEADERS = setOf("date", "datum", "fecha", "data", "created on", "createdon", "transaction date", "transactiondate", "日期", "日付", "дата", "tarih", "tanggal", "날짜", "दिनांक", "تاريخ")
            .map(::normalize).toSet()
        private val TIME_HEADERS = setOf("time", "uhrzeit", "hora", "heure", "ora", "czas", "время", "saat", "thoi gian", "시간", "الوقت")
            .map(::normalize).toSet()
        private val TYPE_HEADERS = setOf("transactiontype", "transaction", "type", "typ", "tipodetransaccion", "tipodetransação", "типоперации", "交易类型", "取引タイプ", "거래유형", "loạigiaodịch", "işlemtürü", "операции")
            .map(::normalize).toSet()
        private val CATEGORY_HEADERS = setOf("category", "kategorie", "categoría", "categoria", "catégorie", "カテゴリ", "категория", "分类", "카테고리", "danh mục", "kategori", "श्रेणी", "الفئة")
            .map(::normalize).toSet()
        private val CATEGORY_TYPE_HEADERS = setOf("categorytype", "kategorietyp", "tipodecategoría", "tipodecategoria", "typedecatégorie", "カテゴリタイプ", "типкатегории", "分类类型", "카테고리유형", "loạidanhmục", "kategoritürü")
            .map(::normalize).toSet()
        private val FROM_ACCOUNT_HEADERS = setOf("fromaccount", "account", "konto", "vonkonto", "cuentadeorigen", "contadeorigem", "comptesource", "dalconto", "z konta", "счётсписания", "счетсписания", "转出账户", "振替元アカウント", "출금계좌", "từtàikhoản", "kaynakhesap", "منحساب")
            .map(::normalize).toSet()
        private val ACCOUNT_TYPE_HEADERS = setOf("accounttype", "kontotyp", "tipodecuenta", "tipodeconta", "typedécompte", "tipodiconto", "typkonta", "типсчёта", "типсчета", "账户类型", "アカウントタイプ", "계좌유형", "loạitàikhoản", "hesaptürü", "نوعالحساب")
            .map(::normalize).toSet()
        private val TO_ACCOUNT_HEADERS = setOf("toaccount", "zukonto", "cuentadedestino", "contadedestino", "comptededestination", "alconto", "na konto", "счётзачисления", "счетзачисления", "转入账户", "振替先アカウント", "입금계좌", "đếntàikhoản", "hedefhesap", "إلىحساب")
            .map(::normalize).toSet()
        private val AMOUNT_HEADERS = setOf("amount", "value", "price", "betrag", "importe", "montant", "tutar", "kwota", "valor", "сумма", "金额", "금액", "số tiền", "المبلغ", "मात्रा")
            .map(::normalize).toSet()
        private val NOTES_HEADERS = setOf("notes", "note", "description", "comment", "notizen", "notas", "notatki", "заметки", "备注", "メモ", "메모", "ghi chú", "notlar", "الملاحظات")
            .map(::normalize).toSet()
        private val ORIGINAL_AMOUNT_HEADERS = setOf("original amount", "originalamount", "原始金额", "原币金额")
            .map(::normalize).toSet()
        private val ORIGINAL_CURRENCY_CODE_HEADERS = setOf("original currency", "originalcurrency", "original currency code", "originalcurrencycode", "原始币种", "原币种")
            .map(::normalize).toSet()
        private val ORIGINAL_CURRENCY_SYMBOL_HEADERS = setOf("original currency symbol", "originalcurrencysymbol", "原始币种符号", "原币符号")
            .map(::normalize).toSet()
        private val ORIGINAL_CURRENCY_NAME_HEADERS = setOf("original currency name", "originalcurrencyname", "原始币种名称", "原币名称")
            .map(::normalize).toSet()
        private val BASE_AMOUNT_HEADERS = setOf("base amount", "baseamount", "基准金额")
            .map(::normalize).toSet()
        private val BASE_CURRENCY_CODE_HEADERS = setOf("base currency", "basecurrency", "base currency code", "basecurrencycode", "基准币种", "基准币种代码")
            .map(::normalize).toSet()
        private val BASE_CURRENCY_SYMBOL_HEADERS = setOf("base currency symbol", "basecurrencysymbol", "基准币种符号")
            .map(::normalize).toSet()
        private val BASE_CURRENCY_NAME_HEADERS = setOf("base currency name", "basecurrencyname", "基准币种名称")
            .map(::normalize).toSet()
        private val EXCHANGE_RATE_MICROS_HEADERS = setOf("exchange rate micros", "exchangeratemicros", "汇率整数")
            .map(::normalize).toSet()
        private val EXCHANGE_RATE_SOURCE_HEADERS = setOf("exchange rate source", "exchangeratesource", "汇率来源")
            .map(::normalize).toSet()
        private val SUMMARY_HEADERS = setOf(
            "exportedon", "totaltransactions", "totalincome", "totalexpense", "netbalance",
            "导出时间", "交易总数", "总收入", "总支出", "净余额",
        ).map(::normalize).toSet()

        private val INCOME_VALUES = setOf(
            "income", "revenue", "revenues", "ingreso", "ingresos", "receita", "receitas",
            "entrata", "entrate", "einnahme", "einnahmen", "доход", "доходы", "收入", "収入",
            "수입", "thu nhap", "pemasukan", "دخل", "आय",
        ).map(::normalize).toSet()
        private val EXPENSE_VALUES = setOf(
            "expense", "expenses", "expenditure", "gasto", "gastos", "despesa", "despesas",
            "spesa", "spese", "ausgabe", "ausgaben", "расход", "расходы", "支出", "지출",
            "chi tieu", "مصروف", "مصروفات", "gider", "wydatki", "खर्च",
        ).map(::normalize).toSet()
        private val TRANSFER_VALUES = setOf(
            "transfer", "transfers", "transferencia", "transferencias", "virement", "virements",
            "trasferimento", "übertragung", "перевод", "转账", "振替", "이체", "chuyen khoan",
            "havale", "przelew", "ट्रांसफर",
        ).map(::normalize).toSet()

        private val REGULAR_ACCOUNT_VALUES = setOf("regular", "standard", "normal", "普通", "常规").map(::normalize).toSet()
        private val CREDIT_ACCOUNT_VALUES = setOf("credit", "信用").map(::normalize).toSet()
        private val BANK_CARD_ACCOUNT_VALUES = setOf("bank_card", "bank card", "银行卡").map(::normalize).toSet()
        private val CASH_ACCOUNT_VALUES = setOf("cash", "现金", "現金").map(::normalize).toSet()
        private val WECHAT_ACCOUNT_VALUES = setOf("wechat", "微信").map(::normalize).toSet()
        private val ALIPAY_ACCOUNT_VALUES = setOf("alipay", "支付宝").map(::normalize).toSet()
        private val OTHER_ACCOUNT_VALUES = setOf("other", "其他").map(::normalize).toSet()

        private val CATEGORY_ALIASES = mapOf(
            "clothing" to setOf("clothing", "clothes", "服装", "衣服", "одежда", "ropa", "vêtements", "kleidung", "giyim", "의류"),
            "entertainment" to setOf("entertainment", "娱乐", "娯楽", "развлечения", "entretenimiento", "divertissement", "unterhaltung", "eğlence", "엔터테인먼트"),
            "food" to setOf("food", "餐饮", "食费", "食物", "еда", "alimentación", "alimentation", "essen", "yiyecek", "식비"),
            "health" to setOf("health", "健康", "здоровье", "salud", "santé", "gesundheit", "sağlık", "건강"),
            "leisure" to setOf("leisure", "休闲", "レジャー", "досуг", "ocio", "loisirs", "freizeit", "boş zaman", "여가"),
            "shopping" to setOf("shopping", "购物", "買い物", "покупки", "compras", "achats", "einkaufen", "alışveriş", "쇼핑"),
            "transportation" to setOf("transportation", "交通", "交通費", "транспорт", "transporte", "transport", "transportation", "ulaşım", "교통"),
            "utilities" to setOf("utilities", "水电煤气", "光热费", "коммунальныеуслуги", "serviciospúblicos", "servicespublics", "nebenkosten", "faturalar", "공과금"),
            "salary" to setOf("salary", "工资", "給料", "зарплата", "salario", "salaire", "gehalt", "maaş", "급여"),
            "gift" to setOf("gift", "礼物", "贈り物", "подарок", "regalo", "cadeau", "geschenk", "hediye", "선물"),
            "coupons" to setOf("coupons", "优惠券", "クーポン", "купоны", "cupones", "coupons", "gutscheine", "kuponlar", "쿠폰"),
        ).mapValues { (_, values) -> values.map(::normalize).toSet() }

        fun canonicalCategoryKey(value: String): String? {
            val normalized = normalize(value)
            return CATEGORY_ALIASES.entries.firstOrNull { normalized in it.value }?.key
        }

        private fun normalize(value: String): String {
            val withoutBom = value.removePrefix("\uFEFF").trim()
            return Normalizer.normalize(withoutBom, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .lowercase(Locale.ROOT)
                .replace(Regex("[^\\p{L}\\p{N}]"), "")
        }

        private fun Long.absoluteValueSafely(): Long = if (this == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(this)
    }
}
