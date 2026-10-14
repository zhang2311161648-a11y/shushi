package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.toCompleteDate
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidget
import com.naveenapps.expensemanager.core.designsystem.components.AmountInfoWidgetCompact
import com.naveenapps.expensemanager.core.designsystem.components.DashboardWidgetTitle
import com.naveenapps.expensemanager.core.designsystem.components.EmptyItem
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.IconAndBackgroundView
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getExpenseColor
import com.naveenapps.expensemanager.core.designsystem.ui.utils.getIncomeColor
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.RankingMode
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.WholeAverageData
import org.koin.compose.viewmodel.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@Composable
fun AnalysisGraphScreen(
    viewModel: AnalysisScreenViewModel = koinViewModel(),
    onRecordTransaction: () -> Unit = {},
) {

    val averageData by viewModel.averageData.collectAsState()
    val amountUiState by viewModel.amountUiState.collectAsState()
    val transactionPeriod by viewModel.transactionPeriod.collectAsState()
    val isCompactSummary by viewModel.isCompactSummary.collectAsState()
    val rankingMode by viewModel.rankingMode.collectAsState()
    val statsPeriod by viewModel.statsPeriod.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedWeekStart by viewModel.selectedWeekStart.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val categoryRankings by viewModel.categoryRankings.collectAsState(initial = emptyList())
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val detailSortMode by viewModel.detailSortMode.collectAsState()
    val categoryTransactionRankings by viewModel.categoryTransactionRankings.collectAsState(
        initial = emptyList(),
    )

    val category = selectedCategory
    if (category == null) {
        ChartScreenContent(
            amountUiState = amountUiState,
            transactionPeriod = transactionPeriod,
            averageData = averageData,
            isCompactSummary = isCompactSummary,
            rankingMode = rankingMode,
            statsPeriod = statsPeriod,
            selectedMonth = selectedMonth,
            selectedWeekStart = selectedWeekStart,
            selectedYear = selectedYear,
            categoryRankings = categoryRankings,
            onRankingModeChange = viewModel::selectRankingMode,
            onStatsPeriodChange = viewModel::selectStatsPeriod,
            onMonthChange = viewModel::selectMonth,
            onWeekChange = viewModel::selectWeek,
            onYearChange = viewModel::selectYear,
            onCategoryClick = viewModel::openCategory,
            onRecordTransaction = onRecordTransaction,
        )
    } else {
        CategoryDrillDownContent(
            category = category,
            rankingMode = rankingMode,
            statsPeriod = statsPeriod,
            selectedMonth = selectedMonth,
            selectedWeekStart = selectedWeekStart,
            selectedYear = selectedYear,
            rankings = categoryTransactionRankings,
            sortMode = detailSortMode,
            onStatsPeriodChange = viewModel::selectStatsPeriod,
            onMonthChange = viewModel::selectMonth,
            onWeekChange = viewModel::selectWeek,
            onYearChange = viewModel::selectYear,
            onSortModeChange = viewModel::selectDetailSortMode,
            onRecordTransaction = onRecordTransaction,
        )
    }
}

@Composable
private fun ChartScreenContent(
    amountUiState: ExpenseFlowState,
    transactionPeriod: String,
    averageData: WholeAverageData,
    isCompactSummary: Boolean = false,
    rankingMode: RankingMode = RankingMode.EXPENSE,
    statsPeriod: StatsPeriod = StatsPeriod.MONTH,
    selectedMonth: YearMonth = YearMonth.now(),
    selectedWeekStart: LocalDate = currentCalendarWeekStart(),
    selectedYear: Int = LocalDate.now().year,
    categoryRankings: List<CategoryRankingItem> = emptyList(),
    onRankingModeChange: (RankingMode) -> Unit = {},
    onStatsPeriodChange: (StatsPeriod) -> Unit = {},
    onMonthChange: (YearMonth) -> Unit = {},
    onWeekChange: (LocalDate) -> Unit = {},
    onYearChange: (Int) -> Unit = {},
    onCategoryClick: (CategoryRankingItem) -> Unit = {},
    onRecordTransaction: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatsControls(
            rankingMode = rankingMode,
            statsPeriod = statsPeriod,
            selectedMonth = selectedMonth,
            selectedWeekStart = selectedWeekStart,
            selectedYear = selectedYear,
            onRankingModeChange = onRankingModeChange,
            onStatsPeriodChange = onStatsPeriodChange,
            onMonthChange = onMonthChange,
            onWeekChange = onWeekChange,
            onYearChange = onYearChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        if (categoryRankings.isNotEmpty()) {
            AppCardView(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryPieChart(categoryRankings, onCategoryClick = onCategoryClick)
            }
        } else {
            EmptyItem(
                modifier = Modifier
                    .fillMaxWidth(),
                emptyItemText = stringResource(id = R.string.no_chart_available),
                icon = com.naveenapps.expensemanager.core.designsystem.R.drawable.ic_no_analysis
            )
        }
        StatsCategoryRankingSection(
            rankingMode = rankingMode,
            rankings = categoryRankings,
            onCategoryClick = onCategoryClick,
            onRecordTransaction = onRecordTransaction,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
private fun StatsControls(
    rankingMode: RankingMode,
    statsPeriod: StatsPeriod,
    selectedMonth: YearMonth,
    selectedWeekStart: LocalDate,
    selectedYear: Int,
    onRankingModeChange: (RankingMode) -> Unit,
    onStatsPeriodChange: (StatsPeriod) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onWeekChange: (LocalDate) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(
                RankingMode.EXPENSE to R.string.stats_expense_mode,
                RankingMode.INCOME to R.string.stats_income_mode,
            ).forEachIndexed { index, (mode, labelRes) ->
                SegmentedButton(
                    selected = rankingMode == mode,
                    onClick = { onRankingModeChange(mode) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                    label = {
                        Text(
                            text = stringResource(labelRes),
                            fontWeight = if (rankingMode == mode) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                        )
                    },
                )
            }
        }

        StatsPeriodSelector(
            selectedPeriod = statsPeriod,
            onPeriodChange = onStatsPeriodChange,
        )

        when (statsPeriod) {
            StatsPeriod.WEEK -> StatsWeekSelector(
                selectedWeekStart = selectedWeekStart,
                onWeekChange = onWeekChange,
            )
            StatsPeriod.MONTH -> StatsMonthSelector(
                selectedMonth = selectedMonth,
                onMonthChange = onMonthChange,
            )
            StatsPeriod.YEAR -> StatsYearSelector(
                selectedYear = selectedYear,
                onYearChange = onYearChange,
            )
            StatsPeriod.CURRENT_PERIOD -> Unit
        }
    }
}

@Composable
private fun StatsCategoryRankingSection(
    rankingMode: RankingMode,
    rankings: List<CategoryRankingItem>,
    onCategoryClick: (CategoryRankingItem) -> Unit,
    onRecordTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = if (rankingMode == RankingMode.EXPENSE) {
        R.string.expense_ranking
    } else {
        R.string.income_ranking
    }

    AppCardView(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            DashboardWidgetTitle(
                title = stringResource(titleRes),
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            )

            AnimatedContent(
                targetState = rankings,
                label = "category_ranking_content",
            ) { items ->
                if (items.isEmpty()) {
                    StatsEmptyState(
                        onRecordTransaction = onRecordTransaction,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                } else {
                    Column {
                        items.forEachIndexed { index, item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically { it / 5 },
                            ) {
                                CategoryRankingRow(
                                    item = item,
                                    onClick = { onCategoryClick(item) },
                                )
                            }
                            if (index < items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant
                                        .copy(alpha = 0.35f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryRankingRow(
    item: CategoryRankingItem,
    onClick: () -> Unit,
) {
    val categoryName = item.categoryTitleResId?.let { stringResource(it) }
        ?: item.categoryName
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconAndBackgroundView(
                icon = item.categoryIcon.name,
                iconBackgroundColor = item.categoryIcon.backgroundColor,
                name = categoryName,
                customImagePath = item.categoryIcon.customImagePath,
                modifier = Modifier.size(42.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = categoryName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(12.dp))
            AnimatedContent(
                targetState = item.totalAmount,
                label = "category_amount",
            ) { amount ->
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        item.monthChangeCents?.let { change ->
            Text(
                text = when {
                    change > 0 -> stringResource(R.string.stats_month_increase, item.monthChangeAmount.orEmpty())
                    change < 0 -> stringResource(R.string.stats_month_decrease, item.monthChangeAmount.orEmpty())
                    else -> stringResource(R.string.stats_month_unchanged)
                },
                modifier = Modifier.padding(start = 54.dp),
                style = MaterialTheme.typography.bodySmall,
                color = if (change < 0) androidx.compose.ui.graphics.Color(0xFF43865A) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = String.format("%.2f %%", item.percentage),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = progressColor.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun CategoryDrillDownContent(
    category: CategoryRankingItem,
    rankingMode: RankingMode,
    statsPeriod: StatsPeriod,
    selectedMonth: YearMonth,
    selectedWeekStart: LocalDate,
    selectedYear: Int,
    rankings: List<CategoryTransactionRankingItem>,
    sortMode: RankingSortMode,
    onStatsPeriodChange: (StatsPeriod) -> Unit,
    onMonthChange: (YearMonth) -> Unit,
    onWeekChange: (LocalDate) -> Unit,
    onYearChange: (Int) -> Unit,
    onSortModeChange: (RankingSortMode) -> Unit,
    onRecordTransaction: () -> Unit,
) {
    val title = category.categoryTitleResId?.let { stringResource(it) }
        ?: category.categoryName
    val rankingTitleRes = if (rankingMode == RankingMode.EXPENSE) {
        R.string.category_detail_expense
    } else {
        R.string.category_detail_income
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        StatsPeriodSelector(
            selectedPeriod = statsPeriod,
            onPeriodChange = onStatsPeriodChange,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        when (statsPeriod) {
            StatsPeriod.WEEK -> StatsWeekSelector(
                selectedWeekStart = selectedWeekStart,
                onWeekChange = onWeekChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            StatsPeriod.MONTH -> StatsMonthSelector(
                selectedMonth = selectedMonth,
                onMonthChange = onMonthChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            StatsPeriod.YEAR -> StatsYearSelector(
                selectedYear = selectedYear,
                onYearChange = onYearChange,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            StatsPeriod.CURRENT_PERIOD -> Unit
        }

        AppCardView(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column {
                DashboardWidgetTitle(
                    title = stringResource(rankingTitleRes),
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    listOf(
                        RankingSortMode.AMOUNT to R.string.sort_by_amount,
                        RankingSortMode.TIME to R.string.sort_by_time,
                    ).forEachIndexed { index, (mode, labelRes) ->
                        SegmentedButton(
                            selected = sortMode == mode,
                            onClick = { onSortModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                            label = { Text(text = stringResource(labelRes)) },
                        )
                    }
                }

                AnimatedContent(
                    targetState = rankings,
                    label = "transaction_ranking_content",
                ) { items ->
                    if (items.isEmpty()) {
                        StatsEmptyState(
                            onRecordTransaction = onRecordTransaction,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    } else {
                        Column {
                            items.forEachIndexed { index, item ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + slideInVertically { it / 5 },
                                ) {
                                    CategoryTransactionRankingRow(item = item)
                                }
                                if (index < items.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                            .copy(alpha = 0.35f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsPeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodChange: (StatsPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        listOf(
            StatsPeriod.WEEK to R.string.week,
            StatsPeriod.MONTH to R.string.month,
            StatsPeriod.YEAR to R.string.year,
            StatsPeriod.CURRENT_PERIOD to R.string.stats_current_period,
        ).forEachIndexed { index, (period, labelRes) ->
            SegmentedButton(
                selected = selectedPeriod == period,
                onClick = { onPeriodChange(period) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 4),
                label = { Text(text = stringResource(labelRes)) },
            )
        }
    }
}

@Composable
private fun CategoryTransactionRankingRow(item: CategoryTransactionRankingItem) {
    val progressColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconAndBackgroundView(
                icon = item.categoryIcon.name,
                iconBackgroundColor = item.categoryIcon.backgroundColor,
                name = item.displayName,
                customImagePath = item.categoryIcon.customImagePath,
                modifier = Modifier.size(38.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.createdOn.toCompleteDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = 0.65f),
                )
            }
            Spacer(Modifier.width(12.dp))
            AnimatedContent(
                targetState = item.amount,
                label = "transaction_amount",
            ) { amount ->
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                    ),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinearProgressIndicator(
                progress = { (item.percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = progressColor.copy(alpha = 0.12f),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = String.format("%.2f %%", item.percentage),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = progressColor.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun StatsEmptyState(
    onRecordTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.stats_no_records),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
                .copy(alpha = 0.75f),
        )
        Button(onClick = onRecordTransaction) {
            Text(text = stringResource(R.string.stats_record_transaction))
        }
    }
}

@Composable
fun IncomeExpenseBalanceView(
    expenseFlowState: ExpenseFlowState,
    transactionPeriod: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
) {
    if (isCompact) {
        AmountInfoWidgetCompact(
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
            modifier = modifier,
        )
    } else {
        AmountInfoWidget(
            expenseAmount = expenseFlowState.expense,
            incomeAmount = expenseFlowState.income,
            balanceAmount = expenseFlowState.balance,
            transactionPeriod = transactionPeriod,
            modifier = modifier,
        )
    }
}

@Composable
fun TransactionAverageItem(
    averageData: WholeAverageData,
    modifier: Modifier = Modifier,
) {
    val incomeColor = colorResource(
        id = com.naveenapps.expensemanager.core.common.R.color.green_500,
    )
    val expenseColor = colorResource(
        id = com.naveenapps.expensemanager.core.common.R.color.red_500,
    )
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)

    AppCardView(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            DashboardWidgetTitle(
                title = stringResource(id = R.string.average_and_projected),
            )

            Spacer(Modifier.height(16.dp))

            // ── Column headers ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Period column — same weight as data rows
                Spacer(Modifier.weight(1f))

                Text(
                    text = stringResource(id = com.naveenapps.expensemanager.core.designsystem.R.string.income),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = incomeColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(id = com.naveenapps.expensemanager.core.designsystem.R.string.expense),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = expenseColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(10.dp))

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = 0.35f),
            )

            Spacer(Modifier.height(10.dp))

            // ── Data rows ──────────────────────────────────────────
            AverageRow(
                label = stringResource(id = R.string.day),
                income = averageData.incomeAverageData.perDay,
                expense = averageData.expenseAverageData.perDay,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )

            Spacer(Modifier.height(12.dp))

            AverageRow(
                label = stringResource(id = R.string.week),
                income = averageData.incomeAverageData.perWeek,
                expense = averageData.expenseAverageData.perWeek,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )

            Spacer(Modifier.height(12.dp))

            AverageRow(
                label = stringResource(id = R.string.month),
                income = averageData.incomeAverageData.perMonth,
                expense = averageData.expenseAverageData.perMonth,
                incomeColor = incomeColor,
                expenseColor = expenseColor,
                labelColor = labelColor,
            )
        }
    }
}

@Composable
private fun AverageRow(
    label: String,
    income: String,
    expense: String,
    incomeColor: Color,
    expenseColor: Color,
    labelColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = labelColor,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = income,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
            ),
            color = incomeColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = expense,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
            ),
            color = expenseColor,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatsChartEmptyPage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CategoryPieChart(
    rankings: List<CategoryRankingItem>,
    modifier: Modifier = Modifier,
    onCategoryClick: (CategoryRankingItem) -> Unit = {},
) {
    val totalCents = rankings.sumOf { it.totalAmountCents }.coerceAtLeast(1L)
    val colors = listOf(Color(0xFFE65100), Color(0xFF00838F), Color(0xFF7B1FA2),
        Color(0xFF1565C0), Color(0xFF558B2F), Color(0xFFAD1457), Color(0xFF6D4C41), Color(0xFF455A64))
    val surfaceColor = MaterialTheme.colorScheme.surface
    val visibleItems = rankings.take(5)
    Row(modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(Modifier.size(115.dp)) {
            var angle = -90f
            rankings.forEachIndexed { index, item ->
                val sweep = item.totalAmountCents.toFloat() / totalCents.toFloat() * 360f
                drawArc(colors[index % colors.size], angle, sweep, true)
                angle += sweep
            }
            drawCircle(surfaceColor, size.minDimension * 0.27f)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("分类分布", style = MaterialTheme.typography.titleSmall)
            visibleItems.forEachIndexed { index, item ->
                val name = item.categoryTitleResId?.let { stringResource(it) } ?: item.categoryName
                Row(Modifier.fillMaxWidth().clickable { onCategoryClick(item) },
                    verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors[index % colors.size], CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(name, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("%.1f%%".format(item.totalAmountCents.toDouble() / totalCents * 100),
                        style = MaterialTheme.typography.labelSmall)
                }
            }
            if (rankings.size > 5) {
                Text("另有 ${rankings.size - 5} 个类别", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@AppPreviewsLightAndDarkMode
@Composable
fun AmountSummaryPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        ChartScreenContent(
            amountUiState = ExpenseFlowState(
                income = "$15000.0",
                expense = "$5000.0",
                balance = "$10000.0",
            ),
            transactionPeriod = "This month (Feb 2026)",
            averageData = WholeAverageData(
                expenseAverageData = AverageData(
                    perDay = "10.0$",
                    perWeek = "10.0$",
                    perMonth = "10.0$",
                ),
                incomeAverageData = AverageData(
                    perDay = "10.0$",
                    perWeek = "10.0$",
                    perMonth = "10.0$",
                ),
            ),
        )
    }
}
