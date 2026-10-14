package com.naveenapps.expensemanager.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class AccountBalanceCorrectionRegressionTest {
    private lateinit var db: ExpenseManagerDatabase
    private fun account(id: String, cents: Long, ledger: String = "default") = AccountEntity(id, id,
        AccountType.REGULAR, "#000000", "wallet", cents / 100.0, 0.0, 0, Date(1), Date(1),
        initialBalanceCents = cents, currentBalanceCents = cents, ledgerId = ledger)
    private fun expense(id: String, cents: Long, type: TransactionType = TransactionType.EXPENSE,
        destination: String? = null) = TransactionEntity(id, "test", "food", "a", type, cents / 100.0,
        "", Date(id.last().digitToInt().toLong() * 1000), Date(1), destination, baseAmountCents = cents)
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), ExpenseManagerDatabase::class.java)
            .allowMainThreadQueries().build()
        db.categoryDao().insert(CategoryEntity("food", "food", CategoryType.EXPENSE, "#000000", "food", Date(1), Date(1)))
        db.accountDao().insert(account("a", 50_000))
        db.accountDao().insert(account("b", 0))
        Unit
    }
    @After fun close() { db.close() }
    @Test fun correctionUpdatesSnapshotsAndNextPaymentDoesNotReplayExpenseTwice() = runBlocking {
        val dao = db.transactionDao()
        dao.insertTransaction(expense("expense1", 20_000))
        val stale = db.accountDao().findById("a")!!
        assertTrue(db.accountDao().updateAccountAndBalances(stale, 100_000))
        assertEquals(120_000L, db.accountDao().findById("a")!!.initialBalanceCents)
        assertEquals(100_000L, dao.findById("expense1")!!.balanceAfterTransactionCents)
        dao.insertTransaction(expense("expense2", 1000))
        assertEquals(99_000L, db.accountDao().findById("a")!!.currentBalanceCents)
        assertEquals(99_000L, dao.findById("expense2")!!.balanceAfterTransactionCents)
        dao.deleteTransaction(dao.findById("expense2")!!)
        assertEquals(100_000L, db.accountDao().findById("a")!!.currentBalanceCents)
    }
    @Test fun staleMetadataAndReorderingNeverOverwriteNewPaymentBalance() = runBlocking {
        val stale = db.accountDao().findById("a")!!
        db.transactionDao().insertTransaction(expense("expense1", 1000))
        db.accountDao().updateAccountAndBalances(stale.copy(name = "renamed"), null)
        db.accountDao().reorderAccounts(listOf(stale.copy(sequence = 3)))
        val saved = db.accountDao().findById("a")!!
        assertEquals(49_000L, saved.currentBalanceCents)
        assertEquals("renamed", saved.name)
        assertEquals(3, saved.sequence)
    }
    @Test fun transferDestinationCorrectionKeepsSourceSnapshotAndOtherAccountUntouched() = runBlocking {
        db.transactionDao().insertTransaction(expense("transfer1", 2000, TransactionType.TRANSFER, "b"))
        db.accountDao().updateAccountAndBalances(db.accountDao().findById("b")!!, 10_000)
        assertEquals(8000L, db.accountDao().findById("b")!!.initialBalanceCents)
        assertEquals(48_000L, db.transactionDao().findById("transfer1")!!.balanceAfterTransactionCents)
        assertEquals(48_000L, db.accountDao().findById("a")!!.currentBalanceCents)
        db.transactionDao().insertTransaction(expense("expense2", 1000))
        assertEquals(10_000L, db.accountDao().findById("b")!!.currentBalanceCents)
    }
    @Test fun correctionUsesActualTransactionsEvenIfCurrentBalanceWasAlreadyCorrupt() = runBlocking {
        db.transactionDao().insertTransaction(expense("expense1", 20_000))
        val account = db.accountDao().findById("a")!!
        db.accountDao().update(account.copy(currentBalanceCents = 1, amount = 0.01))
        db.accountDao().updateAccountAndBalances(account, 100_000)
        assertEquals(120_000L, db.accountDao().findById("a")!!.initialBalanceCents)
        assertEquals(100_000L, db.accountDao().findById("a")!!.currentBalanceCents)
    }
    @Test fun wrongLedgerAndDeletedAccountCannotBeRecreated() = runBlocking {
        assertFalse(db.accountDao().updateAccountAndBalances(account("a", 100, "other"), 1000))
        assertFalse(db.accountDao().updateAccountAndBalances(account("missing", 100), 1000))
        assertEquals(50_000L, db.accountDao().findById("a")!!.currentBalanceCents)
    }
}

