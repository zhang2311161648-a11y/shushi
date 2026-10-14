package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.Resource

data class DataImportResult(
    val importedCount: Int,
    val skippedCount: Int,
    val requiresRestart: Boolean = false,
)

interface BackupRepository {
    suspend fun exportFullData(uri: String): Resource<Boolean>

    fun backupData(uri: String?): Resource<Boolean>

    fun restoreData(uri: String?): Resource<Boolean>

    /** Imports transactions from the CSV produced by the app's data export. */
    suspend fun importData(uri: String): Resource<DataImportResult>
}
