package com.naveenapps.expensemanager.feature.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.EditNotifications
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SettingsApplications
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingToggleRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.core.designsystem.utils.ObserveAsEvents
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.BackupRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.feature.language.LanguageDialogView
import com.naveenapps.expensemanager.feature.theme.ColorPaletteDialogView
import com.naveenapps.expensemanager.feature.theme.ThemeDialogView
import com.naveenapps.expensemanager.feature.theme.colorPaletteTitle
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    shareRepository: ShareRepository,
    backupRepository: BackupRepository,
    viewModel: SettingsViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showImportConfirmation by remember { mutableStateOf(false) }
    var restartRequired by remember { mutableStateOf(false) }
    var backupBusy by remember { mutableStateOf(false) }
    val importScope = rememberCoroutineScope()
    val fullExportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) importScope.launch {
            backupBusy = true
            try {
                when (val result = backupRepository.exportFullData(uri.toString())) {
                    is Resource.Success -> Toast.makeText(context, "全部数据已导出：账本、账户、流水、预算、焦虑抚平器、存钱计划、背景和设置", Toast.LENGTH_LONG).show()
                    is Resource.Error -> Toast.makeText(context, "导出失败：${result.exception.message}", Toast.LENGTH_LONG).show()
                }
            } finally { backupBusy = false }
        }
    }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { selectedUri ->
            importScope.launch {
                backupBusy = true
                try {
                when (val result = backupRepository.importData(selectedUri.toString())) {
                    is Resource.Success -> {
                        restartRequired = result.data.requiresRestart
                        Toast.makeText(
                            context,
                            if (result.data.requiresRestart) "完整备份已校验，退出并重新打开后恢复全部数据" else context.getString(
                                R.string.import_success,
                                result.data.importedCount,
                                result.data.skippedCount,
                            ),
                            Toast.LENGTH_LONG,
                        ).show()
                    }

                    is Resource.Error -> {
                        Toast.makeText(
                            context,
                            R.string.import_failed,
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
                } finally { backupBusy = false }
            }
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text(text = stringResource(id = R.string.import_data)) },
            text = { Text("完整备份会恢复全部账本、账户、流水、预算、焦虑抚平器、存钱计划与设置，并保留恢复前副本。校验后需退出应用再重开。旧 CSV 仅追加流水，请先导出当前完整备份。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        importFileLauncher.launch(CSV_MIME_TYPES)
                    },
                ) {
                    Text(text = stringResource(id = R.string.confirm_import))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }

    if (backupBusy) AlertDialog(onDismissRequest = {}, title = { Text("正在处理全部数据") },
        text = { Text("请等待校验与保存完成，不要关闭应用。") }, confirmButton = {})
    if (restartRequired) AlertDialog(onDismissRequest = {}, title = { Text("完整备份已校验") },
        text = { Text("退出后重新打开书史即可恢复全部数据。恢复前副本会自动保留；失败时恢复原数据。") },
        confirmButton = { TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) { Text("退出应用，随后重新打开") } })

    ObserveAsEvents(viewModel.event) {
        when (it) {
            SettingEvent.RateUs -> {
                shareRepository.openRateUs()
            }

            SettingEvent.Backup -> {
                fullExportLauncher.launch("书史-全部数据-${System.currentTimeMillis()}.shushi.zip")
            }

            SettingEvent.Restore -> {
                showImportConfirmation = true
            }

            SettingEvent.ImportData -> {
                showImportConfirmation = true
            }
        }
    }

    SettingsScreenScaffoldView(
        state = state,
        shareRepository = shareRepository,
        onAction = viewModel::processAction
    )
}

private val CSV_MIME_TYPES = arrayOf(
    "text/csv",
    "text/comma-separated-values",
    "application/csv",
    "text/plain",
    "*/*",
)

@Composable
private fun SettingsScreenScaffoldView(
    state: SettingState,
    shareRepository: ShareRepository? = null,
    onAction: (SettingAction) -> Unit,
) {
    if (state.showThemeSelection) {
        ThemeDialogView {
            onAction.invoke(SettingAction.DismissThemeSelection)
        }
    }

    if (state.showLanguageSelection) {
        LanguageDialogView {
            onAction.invoke(SettingAction.DismissLanguageSelection)
        }
    }

    if (state.showPaletteSelection) {
        ColorPaletteDialogView(
            selectedPalette = state.palette,
            customColorHex = state.customColorHex,
            customBgUri = state.customBackgroundImageUri,
            onSelect = { onAction.invoke(SettingAction.SelectPalette(it)) },
            onCustomColorSelected = { onAction.invoke(SettingAction.SelectCustomColor(it)) },
            onCustomBackgroundSelected = { onAction.invoke(SettingAction.SelectCustomBackgroundImage(it)) },
            onDismiss = { onAction.invoke(SettingAction.DismissPaletteSelection) },
        )
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    onAction.invoke(SettingAction.ClosePage)
                },
                title = stringResource(R.string.settings),
            )
        },
    ) { innerPadding ->
            SettingsScreenContent(
                modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
                state = state,
                shareRepository = shareRepository,
                onAction = onAction,
                    )
    }
}

@Composable
private fun SettingsScreenContent(
    modifier: Modifier = Modifier,
    state: SettingState,
    shareRepository: ShareRepository? = null,
    onAction: (SettingAction) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(modifier = Modifier.height(0.dp))

        // Personalization — shapes how the app looks and feels, so it comes first.
        SettingsSection(title = stringResource(R.string.personalization)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowThemeSelection) },
                    title = stringResource(id = R.string.theme),
                    subtitle = if (state.theme != null) {
                        stringResource(id = state.theme.titleResId)
                    } else {
                        stringResource(id = R.string.system_default)
                    },
                    icon = Icons.Outlined.Palette,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowLanguageSelection) },
                    title = stringResource(
                        id = com.naveenapps.expensemanager.feature.language.R.string.language,
                    ),
                    subtitle = if (state.locale != null) {
                        stringResource(id = state.locale.titleResId)
                    } else {
                        stringResource(id = R.string.system_default)
                    },
                    icon = Icons.Outlined.Language,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.ShowPaletteSelection) },
                    title = stringResource(id = R.string.color_palette),
                    subtitle = colorPaletteTitle(state.palette),
                    icon = Icons.Outlined.Palette,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenCurrencyEdit) },
                    title = stringResource(id = R.string.currency),
                    subtitle = "${state.currency.name} (${state.currency.symbol})",
                    icon = Icons.Outlined.Payments,
                    showDivider = true,
                )
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenNavigationOrder) },
                    title = stringResource(id = R.string.navigation_order),
                    subtitle = stringResource(id = R.string.navigation_order_message),
                    icon = Icons.Outlined.DashboardCustomize,
                )
            }
        }

        // Notifications
        SettingsSection(title = stringResource(R.string.notifications)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenNotification) },
                    title = stringResource(id = R.string.reminder_notification),
                    subtitle = stringResource(id = R.string.selected_daily_reminder_time),
                    icon = Icons.Outlined.EditNotifications,
                    showDivider = true,
                )
            }
        }

        // Defaults — affects daily data entry, so it's promoted out of "Advanced".
        SettingsSection(title = stringResource(R.string.defaults)) {
            AppCardView {
                if (state.accounts.isNotEmpty() && state.selectedAccount != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_account),
                        selectedValue = state.selectedAccount.name,
                        icon = Icons.Outlined.AccountBalance,
                        items = state.accounts.map { it.name },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectAccount(state.accounts[index]))
                        },
                    )
                }

                if (state.expenseCategories.isNotEmpty() && state.selectedExpenseCategory != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_expense_category),
                        selectedValue = state.selectedExpenseCategory.titleResId?.let {
                            stringResource(it)
                        } ?: state.selectedExpenseCategory.name,
                        icon = Icons.AutoMirrored.Outlined.TrendingUp,
                        items = state.expenseCategories.map {
                            it.titleResId?.let { resId -> stringResource(resId) } ?: it.name
                        },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectExpenseCategory(state.expenseCategories[index]))
                        },
                    )
                }

                if (state.incomeCategories.isNotEmpty() && state.selectedIncomeCategory != null) {
                    DropdownSettingItem(
                        label = stringResource(id = R.string.default_income_category),
                        selectedValue = state.selectedIncomeCategory.titleResId?.let {
                            stringResource(it)
                        } ?: state.selectedIncomeCategory.name,
                        icon = Icons.AutoMirrored.Outlined.TrendingDown,
                        items = state.incomeCategories.map {
                            it.titleResId?.let { resId -> stringResource(resId) } ?: it.name
                        },
                        onItemSelected = { index ->
                            onAction.invoke(SettingAction.SelectIncomeCategory(state.incomeCategories[index]))
                        },
                    )
                }
                SettingToggleRow(
                    title = stringResource(id = R.string.compact_summary),
                    subtitle = stringResource(id = R.string.compact_summary_message),
                    icon = Icons.Outlined.DashboardCustomize,
                    checked = state.isCompactSummary,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleCompactSummary) },
                )
            }
        }

        SettingsSection(title = stringResource(R.string.smart_category_section)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenSmartCategorySettings) },
                    title = stringResource(id = R.string.smart_category_settings),
                    subtitle = stringResource(id = R.string.smart_category_settings_message),
                    icon = Icons.Outlined.DashboardCustomize,
                )
            }
        }

        // Data & Backup — export, backup, and restore all live together now (previously split
        // between the main screen and "Advanced").
        SettingsSection(title = stringResource(R.string.data_and_backup)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.Backup) },
                    title = "导出全部数据",
                    subtitle = "全部账本、账户、流水、焦虑抚平器、存钱计划与设置",
                    icon = Icons.Outlined.Upload,
                    showDivider = true,
                )
                SettingRow(
                    title = stringResource(id = R.string.import_data),
                    subtitle = stringResource(id = R.string.import_data_message),
                    icon = Icons.Outlined.Restore,
                    showDivider = true,
                    onClick = { onAction.invoke(SettingAction.ImportData) },
                )
                SettingRow(
                    title = stringResource(id = R.string.backup),
                    subtitle = stringResource(id = R.string.backup_message),
                    icon = Icons.Outlined.Backup,
                    showDivider = true,
                    onClick = { onAction.invoke(SettingAction.Backup) },
                )
                SettingRow(
                    title = stringResource(id = R.string.restore),
                    subtitle = stringResource(id = R.string.restore_message),
                    icon = Icons.Outlined.Restore,
                    onClick = { onAction.invoke(SettingAction.Restore) },
                )
            }
        }

        // Security
        SettingsSection(title = stringResource(R.string.security)) {
            AppCardView {
                SettingToggleRow(
                    title = stringResource(id = R.string.app_lock),
                    subtitle = stringResource(id = R.string.app_lock_message),
                    icon = Icons.Outlined.Lock,
                    checked = state.isAppLockEnabled,
                    onCheckedChange = { onAction.invoke(SettingAction.ToggleAppLock) },
                )
            }
        }

        // Advanced — only the genuinely rare, set-once-and-forget items live behind this now.
        SettingsSection(title = stringResource(R.string.advanced)) {
            AppCardView {
                SettingRow(
                    onClick = { onAction.invoke(SettingAction.OpenAdvancedSettings) },
                    title = stringResource(id = R.string.advanced),
                    subtitle = stringResource(id = R.string.advanced_config_message),
                    icon = Icons.Outlined.SettingsApplications,
                )
            }
        }

        // Acknowledgements — informational, so it stays last.
        SettingsSection(title = stringResource(R.string.support)) {
            AppCardView {
                SettingRow(
                    onClick = { shareRepository?.openFeedbackEmail("") },
                    title = stringResource(id = R.string.user_feedback),
                    subtitle = stringResource(id = R.string.user_feedback_message),
                    icon = Icons.Outlined.Email,
                    showDivider = true,
                )
                SettingRow(
                    onClick = {},
                    title = stringResource(id = R.string.developer),
                    subtitle = stringResource(id = R.string.developer_name),
                    icon = Icons.Outlined.Info,
                    showDivider = true,
                    showChevron = false,
                )
                SettingRow(
                    onClick = {},
                    title = stringResource(id = R.string.open_source_projects),
                    subtitle = stringResource(id = R.string.open_source_thanks),
                    icon = Icons.Outlined.FolderOpen,
                    showChevron = false,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Moved here from AdvancedSettingsScreen along with the Defaults section it belongs to.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSettingItem(
    label: String,
    selectedValue: String,
    icon: ImageVector,
    items: List<String>,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier.fillMaxWidth(),
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedValue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.medium,
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item,
                            fontWeight = if (item == selectedValue) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = if (item == selectedValue) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    } else null,
                    onClick = {
                        onItemSelected(index)
                        expanded = false
                    },
                )
            }
        }
    }
}


@AppPreviewsLightAndDarkMode
@Composable
fun SettingsScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        SettingsScreenScaffoldView(
            state = SettingState(
                currency = Currency("$", "US Dollar"),
                theme = null,
                showThemeSelection = false
            ),
            onAction = {}
        )
    }
}
