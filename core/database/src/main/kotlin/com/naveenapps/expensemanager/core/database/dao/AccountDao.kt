package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.model.BalanceCalculationTransaction
import com.naveenapps.expensemanager.core.model.calculateAccountBalances
import com.naveenapps.expensemanager.core.model.toAmountDouble
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao : BaseDao<AccountEntity> {

    @Query("SELECT * FROM account WHERE ledger_id = :ledgerId ORDER BY sequence ASC")
    fun getAccounts(ledgerId: String): Flow<List<AccountEntity>?>

    fun getAccounts(): Flow<List<AccountEntity>?> = getAccounts(DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM account WHERE id = :id AND ledger_id = :ledgerId")
    suspend fun findById(id: String, ledgerId: String): AccountEntity?

    suspend fun findById(id: String): AccountEntity? = findById(id, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM account WHERE ledger_id = :ledgerId ORDER BY sequence ASC")
    suspend fun getAllValues(ledgerId: String): List<AccountEntity>

    @Query("SELECT * FROM `transaction` WHERE ledger_id = :ledgerId ORDER BY created_on ASC, id ASC")
    suspend fun balanceTransactions(ledgerId: String): List<TransactionEntity>

    @Update
    suspend fun updateBalanceSnapshots(transactions: List<TransactionEntity>)

    @Query("UPDATE account SET sequence = :sequence WHERE id = :id AND ledger_id = :ledgerId")
    suspend fun updateSequence(id: String, ledgerId: String, sequence: Int)

    /** Reordering a stale UI list must never overwrite balances changed by a payment. */
    @Transaction
    suspend fun reorderAccounts(accounts: List<AccountEntity>) {
        accounts.forEach { updateSequence(it.id, it.ledgerId, it.sequence) }
    }

    /** A balance correction changes the baseline, not the already recorded income/expense. */
    @Transaction
    suspend fun updateAccountAndBalances(account: AccountEntity, desiredCurrentBalance: Long?): Boolean {
        val stored = findById(account.id, account.ledgerId) ?: return false
        if (desiredCurrentBalance == null) {
            update(account.copy(amount = stored.amount, initialBalanceCents = stored.initialBalanceCents,
                currentBalanceCents = stored.currentBalanceCents, createdOn = stored.createdOn))
            return true
        }
        val transactions = balanceTransactions(account.ledgerId)
        val entries = transactions.map {
            BalanceCalculationTransaction(it.id, it.type, it.baseAmountCents,
                it.fromAccountId, it.toAccountId, it.createdOn.time)
        }
        val net = calculateAccountBalances(emptyMap(), entries).currentBalances[account.id] ?: 0L
        val baseline = Math.subtractExact(desiredCurrentBalance, net)
        val initialBalances = getAllValues(account.ledgerId).associate {
            it.id to if (it.id == account.id) baseline else it.initialBalanceCents
        }
        val calculation = calculateAccountBalances(initialBalances, entries)
        update(account.copy(amount = desiredCurrentBalance.toAmountDouble(), initialBalanceCents = baseline,
            currentBalanceCents = desiredCurrentBalance, createdOn = stored.createdOn))
        // A transfer stores the source balance; destination corrections must not shift it.
        updateBalanceSnapshots(transactions.filter { it.fromAccountId == account.id }.map {
            it.copy(balanceAfterTransactionCents = calculation.balanceAfterTransactions[it.id])
        })
        return true
    }
}
