package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.model.Ledger
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JiangJiLedgerScreen(
    onBack: () -> Unit,
    viewModel: JiangJiLedgerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val archiveLedger = state.ledgers.firstOrNull { it.id == state.archiveLedgerId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.jiangji_ledger_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JiangJiColors.SecondarySurface),
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MenuBook,
                            contentDescription = null,
                            tint = JiangJiColors.Burgundy,
                            modifier = Modifier.size(28.dp),
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = stringResource(R.string.jiangji_ledger_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = stringResource(R.string.jiangji_ledger_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (state.feedbackMessage != null || state.errorMessage != null) {
                item {
                    Text(
                        text = state.feedbackMessage ?: state.errorMessage.orEmpty(),
                        color = if (state.errorMessage == null) JiangJiColors.Burgundy else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(onClick = viewModel::clearMessage),
                    )
                }
            }
            item {
                Button(
                    onClick = viewModel::openCreateEditor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.jiangji_ledger_create))
                }
            }
            item {
                Text(
                    text = stringResource(R.string.jiangji_ledger_list_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(state.ledgers, key = { it.id }) { ledger ->
                LedgerRow(
                    ledger = ledger,
                    selected = ledger.id == state.currentLedgerId,
                    onSelect = { viewModel.switchTo(ledger) },
                    onRename = { viewModel.openRenameEditor(ledger) },
                    onArchive = { viewModel.requestArchive(ledger) },
                )
            }
        }
    }

    if (state.showEditor) {
        AlertDialog(
            onDismissRequest = viewModel::closeEditor,
            title = {
                Text(
                    if (state.editingLedgerId == null) {
                        stringResource(R.string.jiangji_ledger_create)
                    } else {
                        stringResource(R.string.jiangji_ledger_rename)
                    },
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.editorName,
                        onValueChange = viewModel::updateEditorName,
                        label = { Text(stringResource(R.string.jiangji_ledger_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    state.errorMessage?.let { error ->
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::saveEditor, enabled = !state.isSaving) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeEditor, enabled = !state.isSaving) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    archiveLedger?.let { ledger ->
        AlertDialog(
            onDismissRequest = viewModel::dismissArchive,
            title = { Text(stringResource(R.string.jiangji_ledger_archive)) },
            text = { Text(stringResource(R.string.jiangji_ledger_archive_confirm, ledger.name)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmArchive) {
                    Text(stringResource(R.string.jiangji_ledger_archive))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissArchive) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun LedgerRow(
    ledger: Ledger,
    selected: Boolean,
    onSelect: () -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) JiangJiColors.Burgundy.copy(alpha = 0.10f)
            else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (selected) Icons.Outlined.Check else Icons.Outlined.MenuBook,
                contentDescription = null,
                tint = JiangJiColors.Burgundy,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = ledger.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
                if (selected) {
                    Text(
                        text = stringResource(R.string.jiangji_ledger_current),
                        style = MaterialTheme.typography.bodySmall,
                        color = JiangJiColors.Burgundy,
                    )
                }
            }
            BoxWithMenu(
                expanded = menuExpanded,
                onExpandChange = { menuExpanded = it },
                onRename = {
                    menuExpanded = false
                    onRename()
                },
                onArchive = {
                    menuExpanded = false
                    onArchive()
                },
            )
        }
    }
}

@Composable
private fun BoxWithMenu(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onRename: () -> Unit,
    onArchive: () -> Unit,
) {
    androidx.compose.foundation.layout.Box {
        IconButton(onClick = { onExpandChange(true) }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.jiangji_ledger_more))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandChange(false) }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.jiangji_ledger_rename)) },
                onClick = onRename,
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.jiangji_ledger_archive)) },
                onClick = onArchive,
            )
        }
    }
}
