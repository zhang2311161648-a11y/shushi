package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.common.utils.AppCoroutineDispatchers
import com.naveenapps.expensemanager.core.common.utils.fromLocalToUTCTimeStamp
import com.naveenapps.expensemanager.core.data.mappers.toDomainModel
import com.naveenapps.expensemanager.core.data.mappers.toEntityModel
import com.naveenapps.expensemanager.core.database.dao.AccountDao
import com.naveenapps.expensemanager.core.database.dao.CategoryDao
import com.naveenapps.expensemanager.core.database.dao.TransactionDao
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionRelation
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : TransactionRepository {

    override fun getAllTransaction(): Flow<List<Transaction>?> =
        (currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            transactionDao.getAllTransaction(ledgerId)
        } ?: transactionDao.getAllTransaction()).map {
            convertTransactionAndCategory(it)
        }

    override suspend fun findTransactionById(transactionId: String): Resource<Transaction> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val transaction = transactionDao.findById(transactionId, ledgerId)

                if (transaction != null) {
                    Resource.Success(convertTransactionCategoryRelation(transaction))
                } else {
                    Resource.Error(KotlinNullPointerException())
                }
            } catch (e: Exception) {
                Resource.Error(e)
            }
        }

    override suspend fun addTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val response = transactionDao.insertTransaction(
                    transaction.toEntityModel().copy(ledgerId = ledgerId),
                    transaction.attachments,
                )
                Resource.Success(response != -1L)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun updateTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val transactionEntity = transaction.toEntityModel().copy(ledgerId = ledgerId)
                transactionDao.updateTransaction(
                    transactionEntity,
                    transaction.attachments,
                )
                Resource.Success(true)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    override suspend fun deleteTransaction(transaction: Transaction): Resource<Boolean> =
        withContext(dispatchers.io) {
            return@withContext try {
                val ledgerId = currentLedgerRepository?.getCurrentLedgerId() ?: DEFAULT_LEDGER_ID
                val transactionEntity = transaction.toEntityModel().copy(ledgerId = ledgerId)
                val response = transactionDao.deleteTransaction(transactionEntity)
                Resource.Success(response != -1)
            } catch (exception: Exception) {
                Resource.Error(exception)
            }
        }

    private fun convertTransactionAndCategory(
        transactionWithCategory: List<TransactionRelation>?,
    ): MutableList<Transaction> {
        val outputTransactions = mutableListOf<Transaction>()

        if (transactionWithCategory?.isNotEmpty() == true) {
            transactionWithCategory.forEach {
                convertTransactionCategoryRelation(it)?.let { transaction ->
                    outputTransactions.add(transaction)
                }
            }
        }
        return outputTransactions
    }

    private fun convertTransactionCategoryRelation(relation: TransactionRelation): Transaction? {
        return relation.transactionEntity.toDomainModel().apply {
            category = relation.categoryEntity?.toDomainModel() ?: return null
            fromAccount = relation.fromAccountEntity?.toDomainModel() ?: return null
            toAccount = relation.toAccountEntity?.toDomainModel()
            attachments = relation.attachmentEntities.map { it.imagePath }
        }
    }

    private suspend fun convertTransactionCategoryRelation(relation: TransactionEntity): Transaction {
        val transaction = relation.toDomainModel()
        categoryDao.findById(transaction.categoryId, relation.ledgerId)?.let {
            transaction.category = it.toDomainModel()
        }
        accountDao.findById(transaction.fromAccountId, relation.ledgerId)?.let {
            transaction.fromAccount = it.toDomainModel()
        }
        transaction.toAccountId?.let {
            accountDao.findById(it, relation.ledgerId)?.let { accountEntity ->
                transaction.toAccount = accountEntity.toDomainModel()
            }
        }
        transaction.attachments = transactionDao.getTransactionAttachments(transaction.id)
            .map { it.imagePath }
        return transaction
    }

    override fun getAllFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionType: List<Int>,
    ): Flow<List<Transaction>?> {
        val transactions = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            transactionDao.getAllFilteredTransaction(
                accounts,
                categories,
                transactionType,
                ledgerId,
            )
        } ?: transactionDao.getAllFilteredTransaction(accounts, categories, transactionType)
        return transactions.map {
            convertTransactionAndCategory(it)
        }
    }

    override fun getFilteredTransaction(
        accounts: List<String>,
        categories: List<String>,
        transactionType: List<Int>,
        startDate: Long,
        endDate: Long,
    ): Flow<List<Transaction>?> {
        val transactions = currentLedgerRepository?.observeCurrentLedgerId()?.flatMapLatest { ledgerId ->
            transactionDao.getFilteredTransaction(
                accounts,
                categories,
                transactionType,
                startDate.fromLocalToUTCTimeStamp(),
                endDate.fromLocalToUTCTimeStamp(),
                ledgerId,
            )
        } ?: transactionDao.getFilteredTransaction(
            accounts,
            categories,
            transactionType,
            startDate.fromLocalToUTCTimeStamp(),
            endDate.fromLocalToUTCTimeStamp(),
        )
        return transactions.map {
            convertTransactionAndCategory(it)
        }
    }
}
