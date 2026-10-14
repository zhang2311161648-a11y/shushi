package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.model.AccountingPeriodMode
import com.naveenapps.expensemanager.core.model.SalaryCycle
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun JiangJiAccountingPeriodScreen(
    viewModel: JiangJiAccountingPeriodViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    state.pendingMode?.let { pendingMode ->
        AlertDialog(
            onDismissRequest = viewModel::cancelModeChange,
            title = { Text("修改记账周期？") },
            text = {
                Text("修改后不会改变任何历史账目，只会改变首页、预算和图表的统计范围。")
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmModeChange) {
                    Text("确认修改")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelModeChange) {
                    Text("取消")
                }
            },
        )
    }

    if (state.showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.salaryCycleDate.toPickerMillis(),
        )
        DatePickerDialog(
            onDismissRequest = viewModel::hideDatePicker,
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            viewModel.selectSalaryCycleDate(millis.toPickerDate())
                        } ?: viewModel.hideDatePicker()
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDatePicker) {
                    Text("取消")
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (state.showSalaryCycleEditor) {
        SafeModalBottomSheet(
            onDismissRequest = viewModel::closeSalaryCycleEditor,
            modifier = Modifier.imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = "开始新的工资周期",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "每次收到工资后，开始一个新的记账周期。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = formatAccountingDate(state.salaryCycleDate),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("开始日期") },
                        leadingIcon = {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                        },
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(onClick = viewModel::showDatePicker),
                    )
                }
                OutlinedTextField(
                    value = state.salaryCycleNote,
                    onValueChange = viewModel::updateSalaryCycleNote,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注（可选）") },
                    placeholder = { Text("例如：9月工资到账") },
                    minLines = 2,
                    maxLines = 3,
                )
                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Button(
                    onClick = viewModel::saveSalaryCycle,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving,
                ) {
                    Text(if (state.isSaving) "保存中…" else "开始新周期")
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = viewModel::closePage,
                title = "记账周期",
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "选择更符合你收入节奏的统计方式。",
                    modifier = Modifier.padding(horizontal = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                AppCardView {
                    PeriodModeRow(
                        mode = AccountingPeriodMode.CALENDAR_MONTH,
                        title = "自然月",
                        subtitle = "每月1日至月底",
                        selected = state.settings.mode == AccountingPeriodMode.CALENDAR_MONTH,
                        onClick = { viewModel.requestModeChange(AccountingPeriodMode.CALENDAR_MONTH) },
                    )
                    PeriodModeRow(
                        mode = AccountingPeriodMode.FIXED_START_DAY,
                        title = "固定日期周期",
                        subtitle = "每月固定日期开始新的周期",
                        selected = state.settings.mode == AccountingPeriodMode.FIXED_START_DAY,
                        onClick = { viewModel.requestModeChange(AccountingPeriodMode.FIXED_START_DAY) },
                    )
                    if (state.settings.mode == AccountingPeriodMode.FIXED_START_DAY) {
                        OutlinedTextField(
                            value = state.fixedStartDayText,
                            onValueChange = viewModel::updateFixedStartDay,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 68.dp, end = 16.dp, bottom = 14.dp),
                            label = { Text("每月几号开始") },
                            suffix = { Text("日") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                    PeriodModeRow(
                        mode = AccountingPeriodMode.SALARY_CYCLE,
                        title = "工资周期",
                        subtitle = "收到工资后开始新的周期",
                        selected = state.settings.mode == AccountingPeriodMode.SALARY_CYCLE,
                        onClick = { viewModel.requestModeChange(AccountingPeriodMode.SALARY_CYCLE) },
                    )
                }
            }
            item {
                CurrentPeriodCard(state)
            }
            if (state.settings.mode == AccountingPeriodMode.SALARY_CYCLE) {
                item {
                    Button(
                        onClick = viewModel::openSalaryCycleEditor,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Outlined.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("开始新的工资周期")
                    }
                }
                item {
                    Text(
                        text = "历史周期",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                if (state.salaryCycles.isEmpty()) {
                    item {
                        Text(
                            text = "还没有记录工资周期，先添加今天或过去的工资到账日。",
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    items(state.salaryCycles, key = { it.id }) { cycle ->
                        SalaryCycleRow(cycle = cycle, onDelete = { viewModel.deleteSalaryCycle(cycle) })
                    }
                }
            }
            item {
                Text(
                    text = "修改周期只会改变统计范围，不会修改已有交易的日期、金额或分类。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun PeriodModeRow(
    mode: AccountingPeriodMode,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CurrentPeriodCard(state: JiangJiAccountingPeriodState) {
    val period = state.currentPeriod
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = JiangJiColors.SecondarySurface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "当前周期",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    period == null -> "正在读取…"
                    !period.hasDefinedStart -> "尚未开始工资周期"
                    period.isOpenEnded -> "${formatAccountingDate(period.startDate)} - 至今"
                    else -> "${formatAccountingDate(period.startDate)} - ${formatAccountingDate(period.endDate)}"
                },
                style = MaterialTheme.typography.titleLarge,
                color = JiangJiColors.Burgundy,
                fontWeight = FontWeight.SemiBold,
            )
            if (state.settings.mode == AccountingPeriodMode.FIXED_START_DAY) {
                Text(
                    text = "每月${state.settings.fixedStartDay}日开始",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SalaryCycleRow(cycle: SalaryCycle, onDelete: () -> Unit) {
    AppCardView {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatAccountingDate(cycle.startDate),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                cycle.note?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除周期",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private val ACCOUNTING_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendValue(ChronoField.YEAR, 4)
    .appendLiteral('年')
    .appendValue(ChronoField.MONTH_OF_YEAR)
    .appendLiteral('月')
    .appendValue(ChronoField.DAY_OF_MONTH)
    .appendLiteral('日')
    .toFormatter()

private fun formatAccountingDate(date: LocalDate): String = ACCOUNTING_DATE_FORMATTER.format(date)

private fun LocalDate.toPickerMillis(): Long =
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
