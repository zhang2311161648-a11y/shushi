package com.naveenapps.expensemanager.core.repository

import kotlinx.coroutines.flow.Flow

interface FeedbackRepository {

    suspend fun setTransactionCreated(created: Boolean)

    suspend fun setFeedbackDialogShown(shown: Boolean)

    fun shouldShowFeedbackDialog(): Flow<Boolean>

    /**
     * Local-only builds do not collect crash telemetry, so this remains false and is retained
     * only for compatibility with the existing feedback flow.
     */
    fun didCrashOnPreviousExecution(): Boolean
}
