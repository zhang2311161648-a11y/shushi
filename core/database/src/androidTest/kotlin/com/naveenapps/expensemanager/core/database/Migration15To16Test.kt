package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration_15_to_16_test"

@RunWith(AndroidJUnit4::class)
class Migration15To16Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun backfillsLegacyTransactionsAsCnyOneToOne() {
        val db15 = helper.createDatabase(TEST_DB, 15)
        db15.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on)
            VALUES ('cat-1', 'Food', 0, '#6F1D2C', 'restaurant', 1000, 1000)
            """.trimIndent(),
        )
        db15.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on, initial_balance_cents, current_balance_cents)
            VALUES ('acc-1', 'Cash', 0, '#6F1D2C', 'account_balance', 0.0, 0.0,
                    1, 1000, 1000, 0, 0)
            """.trimIndent(),
        )
        db15.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id, balance_after_transaction_cents)
            VALUES ('tx-1', 'old', 'cat-1', 'acc-1', 1, 12.34, '', 1000, 1000, NULL, NULL)
            """.trimIndent(),
        )
        db15.close()

        val db16 = helper.runMigrationsAndValidate(TEST_DB, 16, true, MIGRATION_15_16)
        val cursor = db16.query(
            "SELECT original_amount_cents, original_currency_code, base_amount_cents, " +
                "base_currency_code, exchange_rate_micros, exchange_rate_source " +
                "FROM `transaction` WHERE id = 'tx-1'",
        )
        cursor.moveToFirst()
        assertThat(cursor.getLong(0)).isEqualTo(1_234L)
        assertThat(cursor.getString(1)).isEqualTo("CNY")
        assertThat(cursor.getLong(2)).isEqualTo(1_234L)
        assertThat(cursor.getString(3)).isEqualTo("CNY")
        assertThat(cursor.getLong(4)).isEqualTo(1_000_000L)
        assertThat(cursor.getString(5)).isEqualTo("legacy")
        cursor.close()
        db16.close()
    }
}
