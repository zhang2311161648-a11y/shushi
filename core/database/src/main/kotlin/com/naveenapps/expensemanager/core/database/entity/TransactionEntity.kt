package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toCents
import java.util.Date

@Entity(
    tableName = "transaction",
    indices = [Index(value = ["ledger_id", "created_on"])],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("from_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("to_account_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("category_id"),
            onUpdate = ForeignKey.NO_ACTION,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "notes")
    val notes: String,
    @ColumnInfo(name = "category_id")
    var categoryId: String,
    @ColumnInfo(name = "from_account_id")
    var fromAccountId: String,
    @ColumnInfo(name = "type")
    var type: TransactionType,
    @ColumnInfo(name = "amount")
    var amount: Double,
    @ColumnInfo(name = "image_path")
    var imagePath: String,
    @ColumnInfo(name = "created_on")
    var createdOn: Date,
    @ColumnInfo(name = "updated_on")
    var updatedOn: Date,
    @ColumnInfo(name = "to_account_id")
    var toAccountId: String?,
    @ColumnInfo(name = "balance_after_transaction_cents", defaultValue = "NULL")
    val balanceAfterTransactionCents: Long? = null,
    @ColumnInfo(name = "original_amount_cents", defaultValue = "0")
    val originalAmountCents: Long = amount.toCents(),
    @ColumnInfo(name = "original_currency_code", defaultValue = "'CNY'")
    val originalCurrencyCode: String = "CNY",
    @ColumnInfo(name = "original_currency_symbol", defaultValue = "'¥'")
    val originalCurrencySymbol: String = "¥",
    @ColumnInfo(name = "original_currency_name", defaultValue = "'Chinese Yuan'")
    val originalCurrencyName: String = "Chinese Yuan",
    @ColumnInfo(name = "original_currency_position", defaultValue = "0")
    val originalCurrencyPosition: Int = 0,
    @ColumnInfo(name = "base_amount_cents", defaultValue = "0")
    val baseAmountCents: Long = amount.toCents(),
    @ColumnInfo(name = "base_currency_code", defaultValue = "'CNY'")
    val baseCurrencyCode: String = "CNY",
    @ColumnInfo(name = "base_currency_symbol", defaultValue = "'¥'")
    val baseCurrencySymbol: String = "¥",
    @ColumnInfo(name = "base_currency_name", defaultValue = "'Chinese Yuan'")
    val baseCurrencyName: String = "Chinese Yuan",
    @ColumnInfo(name = "base_currency_position", defaultValue = "0")
    val baseCurrencyPosition: Int = 0,
    @ColumnInfo(name = "exchange_rate_micros", defaultValue = "1000000")
    val exchangeRateMicros: Long = 1_000_000L,
    @ColumnInfo(name = "exchange_rate_source", defaultValue = "'legacy'")
    val exchangeRateSource: String = "legacy",
    @ColumnInfo(name = "exchange_rate_updated_at", defaultValue = "NULL")
    val exchangeRateUpdatedAt: Long? = null,
    @ColumnInfo(name = "ledger_id", defaultValue = "'default'")
    val ledgerId: String = com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID,
)
