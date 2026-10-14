package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.naveenapps.expensemanager.core.data.importer.CsvImportParser
import com.naveenapps.expensemanager.core.database.ExpenseManagerDatabase
import com.naveenapps.expensemanager.core.database.entity.AccountEntity
import com.naveenapps.expensemanager.core.database.entity.CategoryEntity
import com.naveenapps.expensemanager.core.database.entity.TransactionEntity
import com.naveenapps.expensemanager.core.model.AccountType
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.toAmountDouble
import com.naveenapps.expensemanager.core.model.toCents
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.DataImportResult
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import com.opencsv.CSVReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.Normalizer
import java.util.Date
import java.util.Locale
import java.util.UUID

class BackupRepositoryImpl(
    private val roomBackup: RoomBackup,
    private val database: ExpenseManagerDatabase,
    private val context: Context,
    private val currentLedgerRepository: CurrentLedgerRepository? = null,
) : BackupRepository {

    override suspend fun exportFullData(uri: String): Resource<Boolean> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(Uri.parse(uri), "wt")?.use {
                FullDataBackup.export(context.applicationContext, database, it)
            } ?: throw IOException("无法创建完整备份文件")
            Resource.Success(true)
        } catch (error: Exception) { Resource.Error(error) }
    }

    override fun backupData(uri: String?): Resource<Boolean> {
        roomBackup
            .database(database)
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "$message $exitCode")
                    if (success) {
                        restartApp(Intent(context, LAUNCHER))
                    }
                }
            }
            .backup()

        return Resource.Success(true)
    }

    override fun restoreData(uri: String?): Resource<Boolean> {
        return restoreDatabase()
    }

    override suspend fun importData(uri: String): Resource<DataImportResult> {
        val isArchive = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.read() == 0x50 && it.read() == 0x4b } ?: false
        }
        if (isArchive) return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val count = context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                    FullDataBackup.stage(context.applicationContext, it)
                } ?: throw IOException("无法打开完整备份")
                Resource.Success(DataImportResult(count, 0, requiresRestart = true))
            } catch (error: Exception) { Resource.Error(error) }
        }
        return try {
            val parseResult = context.contentResolver.openInputStream(Uri.parse(uri))?.use { input ->
                CSVReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
                    CsvImportParser().parse(reader.readAll())
                }
            } ?: throw IOException("Unable to open the selected CSV file")

            if (parseResult.transactions.isEmpty() && !parseResult.hasTransactionHeader) {
                throw IllegalArgumentException("The selected file is not a ShuShi CSV export")
            }

            val importBatch = importTransactions(parseResult.transactions)
            Resource.Success(
                DataImportResult(
                    importedCount = importBatch.importedCount,
                    skippedCount = parseResult.skippedCount + importBatch.duplicateCount,
                ),
            )
        } catch (exception: Exception) {
            Log.e(TAG, "CSV import failed", exception)
            Resource.Error(exception)
        }
    }

    private suspend fun importTransactions(
        importedTransactions: List<com.naveenapps.expensemanager.core.data.importer.ImportedTransaction>,
    ): ImportBatchResult {
        if (importedTransactions.isEmpty()) return ImportBatchResult(0, 0)

        val categoryDao = database.categoryDao()
        val accountDao = database.accountDao()
        val transactionDao = database.transactionDao()
        val ledgerId = currentLedgerRepository?.getCurrentLedgerId()
            ?: com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID
        val categories = categoryDao.getAllValues(ledgerId).orEmpty().toMutableList()
        val accounts = transactionDao.getAllAccountEntities(ledgerId).toMutableList()
        val existingTransactionFingerprints = transactionDao.getAllTransactionEntities(ledgerId)
            .mapTo(mutableSetOf(), ::transactionFingerprint)
        val newCategories = mutableListOf<CategoryEntity>()
        val newAccounts = mutableListOf<AccountEntity>()
        val transactionEntities = mutableListOf<TransactionEntity>()
        var duplicateCount = 0

        importedTransactions.forEach { imported ->
            val category = findOrCreateCategory(imported, categories, newCategories, ledgerId)
            val fromAccount = findOrCreateAccount(
                name = imported.fromAccountName,
                type = imported.fromAccountType,
                accounts = accounts,
                newAccounts = newAccounts,
                ledgerId = ledgerId,
            )
            val toAccount = imported.toAccountName
                ?.takeIf { imported.type == TransactionType.TRANSFER }
                ?.let { name ->
                    findOrCreateAccount(
                        name = name,
                        type = imported.toAccountType,
                        accounts = accounts,
                        newAccounts = newAccounts,
                        ledgerId = ledgerId,
                    )
                }

            val transaction = TransactionEntity(
                id = UUID.randomUUID().toString(),
                notes = imported.notes,
                categoryId = category.id,
                fromAccountId = fromAccount.id,
                type = imported.type,
                amount = imported.originalAmountCents.toAmountDouble(),
                imagePath = "",
                createdOn = imported.date,
                updatedOn = imported.date,
                toAccountId = toAccount?.id,
                originalAmountCents = imported.originalAmountCents,
                originalCurrencyCode = imported.originalCurrencyCode,
                originalCurrencySymbol = imported.originalCurrencySymbol,
                originalCurrencyName = imported.originalCurrencyName,
                baseAmountCents = imported.baseAmountCents,
                baseCurrencyCode = imported.baseCurrencyCode,
                baseCurrencySymbol = imported.baseCurrencySymbol,
                baseCurrencyName = imported.baseCurrencyName,
                exchangeRateMicros = imported.exchangeRateMicros,
                exchangeRateSource = imported.exchangeRateSource,
                ledgerId = ledgerId,
            )
            if (existingTransactionFingerprints.add(transactionFingerprint(transaction))) {
                transactionEntities += transaction
            } else {
                duplicateCount++
            }
        }

        for (category in newCategories) {
            categoryDao.insert(category)
        }
        for (account in newAccounts) {
            accountDao.insert(account)
        }
        return ImportBatchResult(
            importedCount = transactionDao.insertTransactions(transactionEntities),
            duplicateCount = duplicateCount,
        )
    }

    private fun transactionFingerprint(transaction: TransactionEntity): String {
        return listOf(
            transaction.type.name,
            transaction.categoryId,
            transaction.fromAccountId,
            transaction.toAccountId.orEmpty(),
            transaction.baseAmountCents,
            transaction.createdOn.time / MILLIS_PER_MINUTE,
            transaction.notes.trim(),
        ).joinToString("|")
    }

    private fun findOrCreateCategory(
        imported: com.naveenapps.expensemanager.core.data.importer.ImportedTransaction,
        categories: MutableList<CategoryEntity>,
        newCategories: MutableList<CategoryEntity>,
        ledgerId: String,
    ): CategoryEntity {
        val type = imported.categoryType ?: imported.type.toCategoryType()
        val normalizedName = normalizeLookup(imported.categoryName)
        val canonicalKey = CsvImportParser.canonicalCategoryKey(imported.categoryName)
        val existing = categories.firstOrNull { category ->
            category.type == type && (
                normalizeLookup(category.name) == normalizedName ||
                    (canonicalKey != null && category.defaultCategoryKey == canonicalKey)
                )
        }
        if (existing != null) return existing

        val created = CategoryEntity(
            id = UUID.randomUUID().toString(),
            name = imported.categoryName,
            type = type,
            iconBackgroundColor = if (type == CategoryType.INCOME) "#688261" else "#6F1D2C",
            iconName = "account_balance",
            updatedOn = Date(),
            ledgerId = ledgerId,
            createdOn = Date(),
        )
        categories += created
        newCategories += created
        return created
    }

    private fun findOrCreateAccount(
        name: String,
        type: AccountType?,
        accounts: MutableList<AccountEntity>,
        newAccounts: MutableList<AccountEntity>,
        ledgerId: String,
    ): AccountEntity {
        val normalizedName = normalizeLookup(name)
        val existing = accounts.firstOrNull { account ->
            normalizeLookup(account.name) == normalizedName && (type == null || account.type == type)
        } ?: accounts.firstOrNull { account -> normalizeLookup(account.name) == normalizedName }
        if (existing != null) return existing

        val now = Date()
        val created = AccountEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type ?: AccountType.REGULAR,
            iconBackgroundColor = "#6F1D2C",
            iconName = "account_balance",
            amount = 0.0,
            creditLimit = 0.0,
            sequence = (accounts.maxOfOrNull { it.sequence } ?: 0) + 1,
            createdOn = now,
            updatedOn = now,
            initialBalanceCents = 0L,
            currentBalanceCents = 0L,
            ledgerId = ledgerId,
        )
        accounts += created
        newAccounts += created
        return created
    }

    private fun TransactionType.toCategoryType(): CategoryType = when (this) {
        TransactionType.INCOME -> CategoryType.INCOME
        TransactionType.EXPENSE, TransactionType.TRANSFER -> CategoryType.EXPENSE
    }

    private fun normalizeLookup(value: String): String {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N}]"), "")
    }

    private data class ImportBatchResult(
        val importedCount: Int,
        val duplicateCount: Int,
    )

    private fun restoreDatabase(): Resource<Boolean> {
        roomBackup
            .database(database)
            .enableLogDebug(true)
            .backupIsEncrypted(true)
            .customEncryptPassword("YOUR_SECRET_PASSWORD")
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(TAG, "$message $exitCode")
                    if (success) {
                        restartApp(Intent(context, LAUNCHER))
                    }
                }
            }
            .restore()
        return Resource.Success(true)
    }

    companion object {
        private const val TAG = "Backup"
        private const val MILLIS_PER_MINUTE = 60_000L
        private val LAUNCHER = Class.forName("com.naveenapps.expensemanager.MainActivity")
    }
}
