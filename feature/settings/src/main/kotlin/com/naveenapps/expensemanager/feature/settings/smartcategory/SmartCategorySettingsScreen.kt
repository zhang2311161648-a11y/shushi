package com.naveenapps.expensemanager.feature.settings.smartcategory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingToggleRow
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingsSection
import com.naveenapps.expensemanager.feature.settings.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SmartCategorySettingsScreen(
    viewModel: SmartCategorySettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.processAction(SmartCategorySettingsAction.DismissClearDialog)
            },
            title = { Text(stringResource(R.string.clear_learned_rules)) },
            text = { Text(stringResource(R.string.clear_learned_rules_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.processAction(SmartCategorySettingsAction.ClearRules) },
                ) {
                    Text(stringResource(R.string.clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.processAction(SmartCategorySettingsAction.DismissClearDialog)
                    },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    viewModel.processAction(SmartCategorySettingsAction.ClosePage)
                },
                title = stringResource(R.string.smart_category_settings),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier.height(0.dp))
            AppCardView {
                SettingToggleRow(
                    title = stringResource(R.string.smart_category_enabled),
                    subtitle = stringResource(R.string.smart_category_enabled_message),
                    icon = Icons.Outlined.Lightbulb,
                    checked = state.isEnabled,
                    onCheckedChange = {
                        viewModel.processAction(SmartCategorySettingsAction.ToggleEnabled)
                    },
                )
            }

            AppCardView {
                Text(
                    text = stringResource(R.string.smart_category_privacy),
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsSection(title = stringResource(R.string.my_category_learning)) {
                AppCardView {
                    if (state.learnedRules.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_learned_rules),
                            modifier = Modifier.padding(16.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.learnedRules.forEachIndexed { index, learnedRule ->
                            SettingRow(
                                title = learnedRule.rule.pattern,
                                subtitle = learnedRule.category?.let {
                                    it.titleResId?.let { resId -> stringResource(resId) } ?: it.name
                                } ?: "—",
                                icon = Icons.Outlined.Info,
                                onClick = {},
                                showDivider = index < state.learnedRules.lastIndex,
                                showChevron = false,
                                trailingContent = {
                                    androidx.compose.material3.IconButton(
                                        onClick = {
                                            viewModel.processAction(
                                                SmartCategorySettingsAction.DeleteRule(learnedRule.rule.id),
                                            )
                                        },
                                    ) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = stringResource(R.string.clear),
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }

            if (state.learnedRules.isNotEmpty()) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.processAction(SmartCategorySettingsAction.ShowClearDialog)
                    },
                ) {
                    Text(stringResource(R.string.clear_learned_rules))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SmartCategorySettingsPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        SmartCategorySettingsScreenContentPreview()
    }
}

@Composable
private fun SmartCategorySettingsScreenContentPreview() {
    // Keep the preview lightweight; the screen is wired through Koin at runtime.
    Text("智能分类建议")
}
