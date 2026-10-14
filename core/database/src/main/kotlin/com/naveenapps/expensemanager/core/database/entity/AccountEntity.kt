package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.toCents
import java.util.Date

@Entity(tableName = "account", indices = [Index(value = ["ledger_id"])])
data class AccountEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: AccountType,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "amount")
    val amount: Double,
    @ColumnInfo(name = "credit_limit")
    val creditLimit: Double,
    @ColumnInfo(name = "sequence")
    val sequence: Int,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
    @ColumnInfo(name = "custom_image_path", defaultValue = "NULL")
    val customImagePath: String? = null,
    @ColumnInfo(name = "initial_balance_cents", defaultValue = "0")
    val initialBalanceCents: Long = amount.toCents(),
    @ColumnInfo(name = "current_balance_cents", defaultValue = "0")
    val currentBalanceCents: Long = amount.toCents(),
    @ColumnInfo(name = "ledger_id", defaultValue = "'default'")
    val ledgerId: String = com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID,
)
