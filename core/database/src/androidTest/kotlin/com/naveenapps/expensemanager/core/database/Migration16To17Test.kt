package com.naveenapps.expensemanager.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val TEST_DB = "migration_16_to_17_test"

@RunWith(AndroidJUnit4::class)
class Migration16To17Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ExpenseManagerDatabase::class.java,
    )

    @Test
    @Throws(IOException::class)
    fun assignsExistingRowsToTheDefaultLedger() {
        val db16 = helper.createDatabase(TEST_DB, 16)
        db16.execSQL(
            """
            INSERT INTO category
                (id, name, type, icon_background_color, icon_name, updated_on, created_on,
                 default_category_key, custom_image_path)
            VALUES ('cat-1', 'Food', 0, '#6F1D2C', 'restaurant', 1000, 1000, NULL, NULL)
            """.trimIndent(),
        )
        db16.execSQL(
            """
            INSERT INTO account
                (id, name, type, icon_background_color, icon_name, amount, credit_limit,
                 sequence, created_on, updated_on, custom_image_path, initial_balance_cents,
                 current_balance_cents)
            VALUES ('acc-1', 'Cash', 0, '#6F1D2C', 'account_balance', 0.0, 0.0,
                    1, 1000, 1000, NULL, 0, 0)
            """.trimIndent(),
        )
        db16.execSQL(
            """
            INSERT INTO `transaction`
                (id, notes, category_id, from_account_id, type, amount, image_path,
                 created_on, updated_on, to_account_id, balance_after_transaction_cents,
                 original_amount_cents, original_currency_code, original_currency_symbol,
                 original_currency_name, original_currency_position, base_amount_cents,
                 base_currency_code, base_currency_symbol, base_currency_name,
                 base_currency_position, exchange_rate_micros, exchange_rate_source,
                 exchange_rate_updated_at)
            VALUES ('tx-1', 'old', 'cat-1', 'acc-1', 1, 12.34, '', 1000, 1000, NULL, NULL,
                    1234, 'CNY', '¥', 'Chinese Yuan', 0, 1234, 'CNY', '¥', 'Chinese Yuan',
                    0, 1000000, 'legacy', NULL)
            """.trimIndent(),
        )
        db16.close()

        val db17 = helper.runMigrationsAndValidate(TEST_DB, 17, true, MIGRATION_16_17)
        val ledgerCursor = db17.query("SELECT id, name, is_archived FROM ledger")
        assertThat(ledgerCursor.count).isEqualTo(1)
        ledgerCursor.moveToFirst()
        assertThat(ledgerCursor.getString(0)).isEqualTo("default")
        assertThat(ledgerCursor.getString(1)).isEqualTo("我的账本")
        assertThat(ledgerCursor.getInt(2)).isEqualTo(0)
        ledgerCursor.close()

        val rowCursor = db17.query(
            "SELECT c.ledger_id, a.ledger_id, t.ledger_id " +
                "FROM category c, account a, `transaction` t " +
                "WHERE c.id = 'cat-1' AND a.id = 'acc-1' AND t.id = 'tx-1'",
        )
        rowCursor.moveToFirst()
        assertThat(rowCursor.getString(0)).isEqualTo("default")
        assertThat(rowCursor.getString(1)).isEqualTo("default")
        assertThat(rowCursor.getString(2)).isEqualTo("default")
        rowCursor.close()
        db17.close()
    }
}
