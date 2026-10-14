package com.naveenapps.expensemanager.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BackupRepository
import kotlinx.coroutines.launch

@androidx.compose.runtime.Composable
internal fun FullBackupExportDialog(repository: BackupRepository, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) scope.launch {
            busy = true
            try {
                message = when (val result = repository.exportFullData(uri.toString())) {
                    is Resource.Success -> "全部数据导出成功。可在设置中的导入数据入口恢复。"
                    is Resource.Error -> "导出失败：${result.exception.message}"
                }
            } finally { busy = false }
        }
    }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("导出全部数据") },
        text = { Text(if (busy) "正在生成完整备份，请等待……" else message ?: "导出全部账本、账户、流水、类别、预算、焦虑抚平器、存钱计划、储蓄、图片、背景、分类习惯与设置。备份含私人记录，请妥善保存。") },
        confirmButton = { TextButton(enabled = !busy, onClick = { launcher.launch("书史-全部数据-${System.currentTimeMillis()}.shushi.zip") }) { Text("选择位置并导出") } },
        dismissButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("关闭") } })
}
