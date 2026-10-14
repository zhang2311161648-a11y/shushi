package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.ColorRes
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.res.colorResource
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconOrCustomImage
import com.naveenapps.expensemanager.core.model.TransactionType
import com.naveenapps.expensemanager.core.model.TransactionUiItem
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.feature.dashboard.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun JiangJiDetailsScreen(
    onOpenTransaction: (String?) -> Unit,
    onAddTransaction: () -> Unit,
    onOpenAccounts: () -> Unit = {},
    onOpenAccountingPeriod: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val today = rememberToday()
    val fiveDaysTransactions = state.transactions.filter { item ->
        item.createdOn?.isWithinPastDays(today, 5) == true
    }
    val currentBudget = state.budgets.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.labelLarge,
                    color = JiangJiColors.Burgundy,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.jiangji_today_summary),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onOpenAccountingPeriod)
                            .padding(horizontal = 7.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = stringResource(R.string.jiangji_accounting_period),
                            tint = JiangJiColors.Burgundy,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = stringResource(R.string.jiangji_accounting_period),
                            style = MaterialTheme.typography.labelMedium,
                            color = JiangJiColors.Burgundy,
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = today.toJiangJiDateLabel(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SummaryCard(
                income = state.expenseFlowState.income,
                expense = state.expenseFlowState.expense,
                balance = state.expenseFlowState.balance,
                budget = currentBudget,
                title = stringResource(
                    if (state.isCustomAccountingPeriod) {
                        R.string.jiangji_current_period_overview
                    } else {
                        R.string.jiangji_current_month_overview
                    },
                ),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.jiangji_today_summary),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.jiangji_view_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = JiangJiColors.Burgundy,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onOpenTransaction(null) }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }

        if (fiveDaysTransactions.isEmpty()) {
            item {
                EmptyTodayState(onAddTransaction = onAddTransaction)
            }
        } else {
            items(fiveDaysTransactions, key = { it.id }) { transaction ->
                JiangJiTransactionRow(
                    transaction = transaction,
                    today = today,
                    onClick = { onOpenTransaction(transaction.id) },
                )
            }
        }
    }
}

@Composable
internal fun AccountBalanceSection(
    accounts: List<AccountUiModel>,
    totalBalance: String,
    onOpenAccounts: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.jiangji_account_balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.jiangji_view_all),
                    style = MaterialTheme.typography.labelLarge,
                    color = JiangJiColors.Burgundy,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenAccounts)
                        .padding(horizontal = 8.dp, vertical = 7.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.jiangji_total_balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = totalBalance.ifBlank { "0" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = JiangJiColors.Burgundy,
                )
            }

            if (accounts.isEmpty()) {
                Text(
                    text = stringResource(R.string.jiangji_no_accounts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        accounts.take(MAX_ACCOUNT_PREVIEW_COUNT).forEach { account ->
                            AccountBalanceChip(account, Modifier.weight(1f))
                        }
                    }
                    if (accounts.size > MAX_ACCOUNT_PREVIEW_COUNT) {
                            Text(
                                text = stringResource(R.string.jiangji_more_accounts),
                                style = MaterialTheme.typography.labelMedium,
                                color = JiangJiColors.Burgundy,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClick = onOpenAccounts)
                                    .padding(horizontal = 10.dp, vertical = 11.dp),
                            )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountBalanceChip(account: AccountUiModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        IconOrCustomImage(
            icon = account.storedIcon.name,
            customImagePath = account.storedIcon.customImagePath,
            contentDescription = account.name,
            tint = JiangJiColors.Burgundy,
            modifier = Modifier.size(18.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.amount.amountString.orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = JiangJiColors.Burgundy,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun SummaryCard(
    income: String,
    expense: String,
    balance: String,
    budget: com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUiModel?,
    title: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = JiangJiColors.Burgundy),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.78f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SummaryValue(stringResource(R.string.jiangji_income), income, JiangJiColors.Income)
                SummaryValue(stringResource(R.string.jiangji_expense), expense, Color(0xFFF1C6C9))
                SummaryValue(stringResource(R.string.jiangji_budget), budget?.amount?.amountString ?: "—", Color(0xFFFFD59E))
                SummaryValue(stringResource(R.string.jiangji_net_balance), balance, Color.White)
            }

            if (budget != null) {
                val clampedProgress = (budget.percent / 100f).coerceIn(0f, 1f)
                val isOverBudget = budget.percent > 100f
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(
                        progress = clampedProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isOverBudget) Color(0xFFFF8585) else Color(0xFFFFD59E),
                        trackColor = Color.White.copy(alpha = 0.20f),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "${budget.transactionAmount.amountString.orEmpty()} of ${budget.amount.amountString.orEmpty()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.72f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isOverBudget) Color(0x33FF0000) else Color(0x33FFFFFF))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f %%", budget.percent),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOverBudget) Color(0xFFFF9494) else Color.White,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SummaryValue(
    label: String,
    value: String,
    valueColor: Color,
) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.72f),
        )
        Text(
            text = value.ifBlank { "0" },
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun JiangJiTransactionRow(
    transaction: TransactionUiItem,
    today: Date,
    onClick: () -> Unit,
) {
    val isIncome = transaction.transactionType == TransactionType.INCOME
    val accent = if (isIncome) JiangJiColors.Income else JiangJiColors.Burgundy
    val title = transaction.categoryName.ifBlank { stringResource(R.string.uncategorized) }
    val dailyRecordFallback = stringResource(R.string.jiangji_record_daily)
    val amountPrefix = if (isIncome) "+" else "−"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconOrCustomImage(
                icon = transaction.categoryIcon.name,
                customImagePath = transaction.categoryIcon.customImagePath,
                contentDescription = title,
                tint = accent,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    transaction.notes?.takeIf { it.isNotBlank() }?.let { append(it) }
                    transaction.fromAccountName.takeIf { it.isNotBlank() }?.let {
                        if (isNotEmpty()) append(" · ")
                        append(it)
                    }
                    if (isEmpty()) append(dailyRecordFallback)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            transaction.balanceAfterTransaction?.amountString?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = stringResource(R.string.jiangji_balance_value, it),
                    style = MaterialTheme.typography.labelSmall,
                    color = JiangJiColors.Burgundy.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "$amountPrefix${transaction.amount.amountString.orEmpty()}",
                style = MaterialTheme.typography.bodyLarge,
                color = accent,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = transaction.createdOn?.toJiangJiDisplayDateTimeLabel(today).orEmpty(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyTodayState(onAddTransaction: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
            .clickable(onClick = onAddTransaction)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.ReceiptLong,
            contentDescription = null,
            tint = JiangJiColors.MutedGold,
            modifier = Modifier.size(32.dp),
        )
        Text(
            text = stringResource(R.string.jiangji_today_no_records),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.jiangji_first_record),
            style = MaterialTheme.typography.labelLarge,
            color = JiangJiColors.Burgundy,
        )
    }
}

private fun rememberToday(): Date = Date()

private fun Date.isSameCalendarDay(other: Date): Boolean {
    val left = Calendar.getInstance().apply { time = this@isSameCalendarDay }
    val right = Calendar.getInstance().apply { time = other }
    return left.get(Calendar.ERA) == right.get(Calendar.ERA) &&
        left.get(Calendar.YEAR) == right.get(Calendar.YEAR) &&
        left.get(Calendar.DAY_OF_YEAR) == right.get(Calendar.DAY_OF_YEAR)
}

private fun Date.toJiangJiDateLabel(): String =
    SimpleDateFormat("yyyy年M月d日 · EEEE", Locale.SIMPLIFIED_CHINESE).format(this)

private fun Date.isWithinPastDays(today: Date, days: Int): Boolean {
    val cal = Calendar.getInstance().apply {
        time = today
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    val endMs = cal.timeInMillis
    cal.apply {
        time = today
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -(days - 1))
    }
    val startMs = cal.timeInMillis
    return this.time in startMs..endMs
}

private fun Date.toJiangJiTimeLabel(): String =
    SimpleDateFormat("HH:mm", Locale.US).format(this)

private fun Date.toJiangJiDisplayDateTimeLabel(today: Date): String {
    return if (this.isSameCalendarDay(today)) {
        SimpleDateFormat("HH:mm", Locale.US).format(this)
    } else {
        SimpleDateFormat("M月d日 HH:mm", Locale.SIMPLIFIED_CHINESE).format(this)
    }
}

internal const val MAX_ACCOUNT_PREVIEW_COUNT = 3
