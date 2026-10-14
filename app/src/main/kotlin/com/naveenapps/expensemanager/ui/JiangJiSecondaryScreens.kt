package com.naveenapps.expensemanager.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import com.naveenapps.expensemanager.feature.dashboard.DashboardViewModel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.Asset
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

@Composable
fun JiangJiMeScreen(
    onOpenRoute: (JiangJiMeRoute) -> Unit,
    onOpenLedger: () -> Unit = {},
    onImportData: (String) -> Unit = {},
    onSendFeedback: (String) -> Unit = {},
    dashboardViewModel: DashboardViewModel = koinViewModel(),
) {
    val profileViewModel: JiangJiProfileViewModel = koinViewModel()
    val profileState by profileViewModel.state.collectAsState()
    val dashboardState by dashboardViewModel.state.collectAsState()
    var showLanguageSelection by remember { mutableStateOf(false) }
    var showImportConfirmation by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var feedbackMessage by rememberSaveable { mutableStateOf("") }
    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { onImportData(it.toString()) }
    }
    val profileAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(profileViewModel::onAvatarSelected)
    }

    if (profileState.isEditorVisible) {
        AlertDialog(
            onDismissRequest = profileViewModel::dismissEditor,
            title = { Text(text = stringResource(R.string.jiangji_profile_edit)) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    JiangJiProfileAvatar(
                        avatarPath = profileState.draftAvatarPath,
                        modifier = Modifier.size(92.dp),
                        contentDescription = stringResource(R.string.jiangji_profile_edit),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            enabled = !profileState.isAvatarLoading,
                            onClick = { profileAvatarLauncher.launch(arrayOf("image/*")) },
                        ) {
                            if (profileState.isAvatarLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Text(text = stringResource(R.string.jiangji_profile_choose_avatar))
                            }
                        }
                        if (profileState.draftAvatarPath != null) {
                            TextButton(
                                enabled = !profileState.isAvatarLoading,
                                onClick = profileViewModel::removeDraftAvatar,
                            ) {
                                Text(text = stringResource(R.string.jiangji_profile_remove_avatar))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = profileState.draftName,
                        onValueChange = profileViewModel::updateDraftName,
                        label = { Text(text = stringResource(R.string.jiangji_profile_name)) },
                        singleLine = true,
                        isError = profileState.validationError != null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    profileState.validationError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    profileState.avatarError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    profileState.saveError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !profileState.isSaving && !profileState.isAvatarLoading,
                    onClick = profileViewModel::save,
                ) {
                    Text(text = stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = profileViewModel::dismissEditor) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showLanguageSelection) {
        com.naveenapps.expensemanager.feature.language.LanguageDialogView {
            showLanguageSelection = false
        }
    }

    if (showImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showImportConfirmation = false },
            title = { Text(text = stringResource(R.string.jiangji_data_import)) },
            text = { Text(text = stringResource(R.string.jiangji_import_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmation = false
                        importFileLauncher.launch(CSV_MIME_TYPES)
                    },
                ) {
                    Text(text = stringResource(R.string.jiangji_confirm_import))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmation = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showFeedbackDialog) {
        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text(text = stringResource(R.string.jiangji_feedback)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.jiangji_feedback_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.jiangji_feedback_contact),
                        style = MaterialTheme.typography.bodySmall,
                        color = JiangJiColors.Burgundy,
                    )
                    OutlinedTextField(
                        value = feedbackMessage,
                        onValueChange = { feedbackMessage = it },
                        label = { Text(text = stringResource(R.string.jiangji_feedback_hint)) },
                        minLines = 4,
                        maxLines = 7,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = feedbackMessage.isNotBlank(),
                    onClick = {
                        onSendFeedback(feedbackMessage.trim())
                        feedbackMessage = ""
                        showFeedbackDialog = false
                    },
                ) {
                    Text(text = stringResource(R.string.jiangji_feedback_send))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = profileViewModel::openEditor)
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                JiangJiProfileAvatar(
                    avatarPath = profileState.profile.avatarPath,
                    modifier = Modifier.size(64.dp),
                    contentDescription = profileState.profile.displayName,
                )
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = profileState.profile.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(R.string.jiangji_private_local_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.jiangji_slogan),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
                    )
                }
                IconButton(onClick = profileViewModel::openEditor) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.jiangji_profile_edit),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            AccountBalanceSection(
                accounts = dashboardState.accounts,
                totalBalance = dashboardState.totalAccountBalance,
                onOpenAccounts = { onOpenRoute(JiangJiMeRoute.Accounts) },
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenLedger,
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MenuBook,
                        contentDescription = null,
                        tint = JiangJiColors.MutedGold,
                        modifier = Modifier.size(26.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = stringResource(R.string.jiangji_my_ledger),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.jiangji_default_ledger_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = stringResource(R.string.jiangji_open_ledger),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenRoute(JiangJiMeRoute.Settings) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 17.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null,
                        tint = JiangJiColors.Burgundy,
                        modifier = Modifier.size(26.dp),
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.jiangji_settings_summary_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private val CSV_MIME_TYPES = arrayOf(
    "text/csv",
    "text/comma-separated-values",
    "application/csv",
    "text/plain",
    "*/*",
)

@Composable
private fun JiangJiSettingRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = JiangJiColors.Burgundy,
            modifier = Modifier.size(21.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
fun JiangJiLegacyAssetScreen(
    viewModel: JiangJiAssetsViewModel = koinViewModel(),
    anxietyReliefViewModel: JiangJiAnxietyReliefViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var assetToDelete by remember { mutableStateOf<Asset?>(null) }
    var section by remember { mutableStateOf(JiangJiAssetsSection.AnxietyRelief) }

    if (section == JiangJiAssetsSection.AnxietyRelief) {
        Column(modifier = Modifier.fillMaxSize()) {
            JiangJiAssetSectionTabs(
                selected = section,
                onSelected = { section = it },
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
            )
            JiangJiAnxietyReliefScreen(
                viewModel = anxietyReliefViewModel,
                modifier = Modifier.weight(1f),
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            JiangJiAssetSectionTabs(
                selected = section,
                onSelected = { section = it },
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "资产",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "独立记录你的现金、存款和长期资产",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(onClick = viewModel::openNewAsset) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(text = "添加资产")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = JiangJiColors.DeepBurgundy),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = JiangJiColors.MutedGold,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "${state.assets.size} 项",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                    Text(
                        text = "总资产",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.78f),
                    )
                    Text(
                        text = state.formattedTotal,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        if (state.errorMessage != null) {
            item {
                Text(
                    text = state.errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = JiangJiColors.DeepBurgundy,
                    modifier = Modifier.clickable(onClick = viewModel::clearError),
                )
            }
        }

        if (state.assets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 26.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalance,
                            contentDescription = null,
                            tint = JiangJiColors.Burgundy,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = "还没有资产",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "添加第一项资产，开始建立你的资产总览",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = viewModel::openNewAsset) {
                            Text(text = "添加第一项")
                        }
                    }
                }
            }
        } else {
            items(state.assets, key = { it.id }) { asset ->
                JiangJiAssetRow(
                    asset = asset,
                    formattedAmount = state.formattedAmounts[asset.id] ?: asset.amount.toString(),
                    onEdit = { viewModel.openEditAsset(asset) },
                    onDelete = { assetToDelete = asset },
                )
            }
        }

        item {
            Text(
                text = "资产使用独立数据表，不会影响收入、支出、结余、预算或图表。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    state.editor?.let { editor ->
        AlertDialog(
            onDismissRequest = viewModel::closeEditor,
            title = {
                Text(text = if (editor.assetId == null) "添加资产" else "编辑资产")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editor.name,
                        onValueChange = viewModel::updateEditorName,
                        label = { Text(text = "资产名称") },
                        placeholder = { Text(text = "例如：应急储蓄") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = editor.amount,
                        onValueChange = viewModel::updateEditorAmount,
                        label = { Text(text = "当前金额") },
                        placeholder = { Text(text = "请输入金额") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = editor.note,
                        onValueChange = viewModel::updateEditorNote,
                        label = { Text(text = "备注（可选）") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    editor.validationError?.let { error ->
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = JiangJiColors.DeepBurgundy,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::saveEditor,
                    enabled = !editor.isSaving,
                ) {
                    Text(text = if (editor.isSaving) "保存中…" else "保存")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::closeEditor,
                    enabled = !editor.isSaving,
                ) {
                    Text(text = "取消")
                }
            },
        )
    }

    assetToDelete?.let { asset ->
        AlertDialog(
            onDismissRequest = { assetToDelete = null },
            title = { Text(text = "删除资产？") },
            text = { Text(text = "确认删除“${asset.name}”吗？删除后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAsset(asset)
                        assetToDelete = null
                    },
                ) {
                    Text(text = "删除", color = JiangJiColors.DeepBurgundy)
                }
            },
            dismissButton = {
                TextButton(onClick = { assetToDelete = null }) {
                    Text(text = "取消")
                }
            },
        )
    }
}

@Composable
private fun JiangJiProfileAvatar(
    avatarPath: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    val avatarFile = avatarPath?.let(::File)?.takeIf(File::exists)
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(JiangJiColors.Burgundy),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarFile != null) {
            AsyncImage(
                model = avatarFile,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun JiangJiAssetRow(
    asset: Asset,
    formattedAmount: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(JiangJiColors.SecondarySurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = JiangJiColors.Burgundy,
                    modifier = Modifier.size(23.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (asset.note.isNotBlank()) {
                    Text(
                        text = asset.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedAmount,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = JiangJiColors.Burgundy,
                )
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "编辑${asset.name}",
                            tint = JiangJiColors.Burgundy,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "删除${asset.name}",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
            }
        }
    }
}

enum class JiangJiMeRoute {
    Accounts,
    Categories,
    Budgets,
    Export,
    Settings,
    Currency,
    Reminder,
    About,
}
