package com.naveenapps.expensemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionAttachmentEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionRelation
import com.naveenapps.expensemanager.core.model.calculateAccountBalances
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.model.toAmountDouble
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.UUID

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query("SELECT * from `transaction` WHERE id=:id AND ledger_id = :ledgerId")
    fun findById(id: String, ledgerId: String): TransactionEntity?

    fun findById(id: String): TransactionEntity? = findById(id, DEFAULT_LEDGER_ID)
    @Query("SELECT * FROM `transaction` WHERE id=:id")
    suspend fun findByAnyLedgerId(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertNewTransaction(transaction: TransactionEntity): Long

    // @Transaction is required here (not just on writes) because TransactionRelation embeds
    // multiple @Relation fields (category, fromAccount, toAccount, attachments). Room fetches
    // each relation via a separate sub-query and stitches the results back onto the parent rows
    // using a key->list map; without @Transaction that stitching isn't atomic with the main
    // query, so a concurrent write (e.g. attachments being deleted/reinserted on save) between
    // the main query and a relation sub-query can leave a row's key missing from the map and
    // throw NoSuchElementException from the generated getValue(...) call.
    @Transaction
    @Query("SELECT * FROM `transaction` WHERE ledger_id = :ledgerId")
    fun getAllTransaction(ledgerId: String): Flow<List<TransactionRelation>?>

    fun getAllTransaction(): Flow<List<TransactionRelation>?> = getAllTransaction(DEFAULT_LEDGER_ID)

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.ledger_id = :ledgerId
        AND `transaction`.from_account_id IN(:accounts)
        AND `transaction`.category_id IN(:categories)
        AND `transaction`.type IN(:transactionTypes)
        ORDER BY `transaction`.created_on DESC
        """,
    )
    fun getAllFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        ledgerId: String,
    ): Flow<List<TransactionRelation>?>

    fun getAllFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
    ): Flow<List<TransactionRelation>?> = getAllFilteredTransaction(
        accounts,
        categories,
        transactionTypes,
        DEFAULT_LEDGER_ID,
    )

    @Transaction
    @Query(
        """
        SELECT * FROM `transaction`
        WHERE `transaction`.ledger_id = :ledgerId
        AND `transaction`.from_account_id IN(:accounts)
        AND `transaction`.category_id IN(:categories)
        AND `transaction`.type IN(:transactionTypes)
        AND `transaction`.created_on BETWEEN :fromDate AND :toDate
        ORDER BY `transaction`.created_on DESC
        """,
    )
    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        fromDate: Long,
        toDate: Long,
        ledgerId: String,
    ): Flow<List<TransactionRelation>?>

    fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionTypes: List<Int>,
        fromDate: Long,
        toDate: Long,
    ): Flow<List<TransactionRelation>?> = getFilteredTransaction(
        accounts,
        categories,
        transactionTypes,
        fromDate,
        toDate,
        DEFAULT_LEDGER_ID,
    )

    @Query("SELECT * FROM account WHERE id = :id AND ledger_id = :ledgerId")
    suspend fun findAccountById(id: String, ledgerId: String): AccountEntity?

    suspend fun findAccountById(id: String): AccountEntity? = findAccountById(id, DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM account WHERE ledger_id = :ledgerId")
    suspend fun getAllAccountEntities(ledgerId: String): List<AccountEntity>

    suspend fun getAllAccountEntities(): List<AccountEntity> = getAllAccountEntities(DEFAULT_LEDGER_ID)

    @Query("SELECT * FROM `transaction` WHERE ledger_id = :ledgerId ORDER BY created_on ASC, id ASC")
    suspend fun getAllTransactionEntities(ledgerId: String): List<TransactionEntity>

    suspend fun getAllTransactionEntities(): List<TransactionEntity> = getAllTransactionEntities(DEFAULT_LEDGER_ID)

    @Update
    suspend fun updateAllAccountEntities(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAllTransactionEntities(transactions: List<TransactionEntity>)

    @Query(
        "SELECT * FROM transaction_attachment WHERE transaction_id = :transactionId ORDER BY created_on ASC",
    )
    suspend fun getTransactionAttachments(transactionId: String): List<TransactionAttachmentEntity>

    @Query("DELETE FROM transaction_attachment WHERE transaction_id = :transactionId")
    suspend fun removeTransactionAttachments(transactionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionAttachment(attachmentEntity: TransactionAttachmentEntity): Long

    @Transaction
    suspend fun insertTransaction(
        transactionEntity: TransactionEntity,
        attachmentPaths: List<String> = emptyList(),
    ): Long {
        val id = insertNewTransaction(transactionEntity)
        if (id != -1L) {
            attachmentPaths.forEach { path ->
                insertTransactionAttachment(
                    TransactionAttachmentEntity(
                        id = UUID.randomUUID().toString(),
                        transactionId = transactionEntity.id,
                        imagePath = path,
                        createdOn = Date(),
                    ),
                )
            }
            recalculateAccountBalances(transactionEntity.ledgerId)
        }
        return id
    }

    /** Inserts an imported CSV batch and recalculates account balances once. */
    @Transaction
    suspend fun insertTransactions(transactionEntities: List<TransactionEntity>): Int {
        if (transactionEntities.isEmpty()) return 0

        transactionEntities.forEach { transactionEntity ->
            insert(transactionEntity)
        }
        recalculateAccountBalances(transactionEntities.first().ledgerId)
        return transactionEntities.size
    }

    @Transaction
    suspend fun updateTransaction(
        transactionEntity: TransactionEntity,
        attachmentPaths: List<String> = emptyList(),
    ) {
        update(transactionEntity)

        removeTransactionAttachments(transactionEntity.id)
        attachmentPaths.forEach { path ->
            insertTransactionAttachment(
                TransactionAttachmentEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionEntity.id,
                    imagePath = path,
                    createdOn = Date(),
                ),
            )
        }
        recalculateAccountBalances(transactionEntity.ledgerId)
    }

    @Transaction
    suspend fun deleteTransaction(transactionEntity: TransactionEntity): Int {
        val deleted = delete(transactionEntity)
        if (deleted > 0) {
            recalculateAccountBalances(transactionEntity.ledgerId)
        }
        return deleted
    }

    /** Replays every account transaction so edits, deletes and date changes cannot drift. */
    private suspend fun recalculateAccountBalances(ledgerId: String) {
        val accounts = getAllAccountEntities(ledgerId)
        val transactions = getAllTransactionEntities(ledgerId)
        val calculation = calculateAccountBalances(
            initialBalances = accounts.associate { account ->
                account.id to account.initialBalanceCents
            },
            transactions = transactions.map { transaction ->
                com.naveenapps.expensemanager.core.model.BalanceCalculationTransaction(
                    id = transaction.id,
                    type = transaction.type,
                    amountCents = transaction.baseAmountCents,
                    fromAccountId = transaction.fromAccountId,
                    toAccountId = transaction.toAccountId,
                    createdAtMillis = transaction.createdOn.time,
                )
            },
        )

        val updatedAccounts = accounts.map { account ->
            val currentBalance = calculation.currentBalances[account.id]
                ?: account.initialBalanceCents
            account.copy(
                amount = currentBalance.toAmountDouble(),
                currentBalanceCents = currentBalance,
            )
        }
        if (updatedAccounts.isNotEmpty()) {
            updateAllAccountEntities(updatedAccounts)
        }

        val updatedTransactions = transactions.map { transaction ->
            transaction.copy(
                balanceAfterTransactionCents = calculation.balanceAfterTransactions[transaction.id],
            )
        }
        if (updatedTransactions.isNotEmpty()) {
            updateAllTransactionEntities(updatedTransactions)
        }
    }
}
