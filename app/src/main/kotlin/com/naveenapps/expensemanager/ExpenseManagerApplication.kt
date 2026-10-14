package com.naveenapps.expensemanager

import android.app.Application

class ExpenseManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val process = if (android.os.Build.VERSION.SDK_INT >= 28) getProcessName() else {
            (getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
                .runningAppProcesses?.firstOrNull { it.pid == android.os.Process.myPid() }?.processName
        }
        if (process != packageName) return
        // Retire feature-specific state when upgrading, without touching ledger data.
        listOf("local_ai", "payment_sync", "payment_diagnostics").forEach {
            deleteSharedPreferences(it)
        }
        val retiredModels = java.io.File(noBackupFilesDir, "ai-models")
        if (retiredModels.canonicalFile.parentFile == noBackupFilesDir.canonicalFile) {
            retiredModels.deleteRecursively()
        }
    }
    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        val process = if (android.os.Build.VERSION.SDK_INT >= 28) getProcessName() else {
            (base.getSystemService(android.content.Context.ACTIVITY_SERVICE) as android.app.ActivityManager)
                .runningAppProcesses?.firstOrNull { it.pid == android.os.Process.myPid() }?.processName
        }
        if (process == base.packageName) {
            com.naveenapps.expensemanager.core.data.repository.FullDataBackup.applyBeforeStartup(base)
        }
    }
}
