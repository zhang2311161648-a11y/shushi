package com.naveenapps.expensemanager.feature.analysis

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.ObserveAccountingPeriodUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetAllDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.GetDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.daterange.SaveDateRangeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAmountStateUseCase
import com.naveenapps.expensemanager.core.domain.usecase.transaction.GetAverageDataUseCase
import com.naveenapps.expensemanager.core.domain.usecase.stats.GetStatsCategoryRankingUseCase
import com.naveenapps.expensemanager.core.domain.usecase.stats.GetStatsCategoryTransactionRankingUseCase
import com.naveenapps.expensemanager.core.model.CategoryRankingItem
import com.naveenapps.expensemanager.core.model.AverageData
import com.naveenapps.expensemanager.core.model.ExpenseFlowState
import com.naveenapps.expensemanager.core.model.RankingMode
import com.naveenapps.expensemanager.core.model.RankingSortMode
import com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem
import com.naveenapps.expensemanager.core.model.DateRangeModel
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.model.WholeAverageData
import com.naveenapps.expensemanager.core.model.DateRangeType
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import java.time.LocalDate
import java.time.YearMonth


class AnalysisScreenViewModel(
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getAverageDataUseCase: GetAverageDataUseCase,
    getAmountStateUseCase: GetAmountStateUseCase,
    getDateRangeUseCase: GetDateRangeUseCase,
    private val getAllDateRangeUseCase: GetAllDateRangeUseCase,
    private val saveDateRangeUseCase: SaveDateRangeUseCase,
    private val getStatsCategoryRankingUseCase: GetStatsCategoryRankingUseCase,
    private val getStatsCategoryTransactionRankingUseCase: GetStatsCategoryTransactionRankingUseCase,
    settingsRepository: SettingsRepository,
    private val observeAccountingPeriodUseCase: ObserveAccountingPeriodUseCase? = null,
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            R.string.analysis,
        ),
    )
    val currentTheme = _currentTheme.asStateFlow()

    private val _expenseFlowState = MutableStateFlow(ExpenseFlowState())
    val amountUiState = _expenseFlowState.asStateFlow()

    private val _transactionPeriod = MutableStateFlow("")
    val transactionPeriod = _transactionPeriod.asStateFlow()

    private val _isCompactSummary = MutableStateFlow(false)
    val isCompactSummary = _isCompactSummary.asStateFlow()

    private val _rankingMode = MutableStateFlow(RankingMode.EXPENSE)
    val rankingMode = _rankingMode.asStateFlow()

    private val _statsPeriod = MutableStateFlow(StatsPeriod.MONTH)
    val statsPeriod = _statsPeriod.asStateFlow()

    /** Calendar month shown when the Stats period is MONTH. */
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    /** Monday that starts the calendar week shown when the Stats period is WEEK. */
    private val _selectedWeekStart = MutableStateFlow(currentCalendarWeekStart())
    val selectedWeekStart = _selectedWeekStart.asStateFlow()

    /** Calendar year shown when the Stats period is YEAR. */
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear = _selectedYear.asStateFlow()

    // DateRangeDataStore persists the range and type in two writes. Ignore the
    // intermediate emission so a custom payroll period cannot immediately
    // overwrite the period the user just selected.
    private var pendingStatsPeriod: StatsPeriod? = null

    private val _selectedCategory = MutableStateFlow<CategoryRankingItem?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _detailSortMode = MutableStateFlow(RankingSortMode.AMOUNT)
    val detailSortMode = _detailSortMode.asStateFlow()

    private val statsSelection: Flow<StatsSelection> = combine(
        _statsPeriod,
        _selectedMonth,
        _selectedWeekStart,
        _selectedYear,
    ) { period, month, weekStart, year ->
        StatsSelection(period, month, weekStart, year)
    }

    val categoryRankings: Flow<List<CategoryRankingItem>> = combine(
        _rankingMode,
        statsSelection,
        observeAccountingPeriodUseCase?.invoke() ?: kotlinx.coroutines.flow.flowOf(null),
    ) { mode, selection, accountingPeriod ->
        val range = selection.period.dateRange(
            accountingPeriod = accountingPeriod,
            selectedMonth = selection.month,
            selectedWeekStart = selection.weekStart,
            selectedYear = selection.year,
        )
        val previousMonth = if (selection.period == StatsPeriod.MONTH) selection.month.minusMonths(1).toDateRange() else null
        Triple(mode, range, previousMonth)
    }.flatMapLatest { (mode, range, previousMonth) ->
        getStatsCategoryRankingUseCase.invoke(
            mode = mode,
            startDate = range.first(),
            endDate = range.last(),
            comparisonStartDate = previousMonth?.first(),
            comparisonEndDate = previousMonth?.last(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val categoryTransactionRankings: Flow<List<com.naveenapps.expensemanager.core.model.CategoryTransactionRankingItem>> = combine(
        _selectedCategory,
        statsSelection,
        _rankingMode,
        _detailSortMode,
        observeAccountingPeriodUseCase?.invoke() ?: kotlinx.coroutines.flow.flowOf(null),
    ) { category, selection, mode, sortMode, accountingPeriod ->
        Quintuple(
            category,
            selection.period.dateRange(
                accountingPeriod = accountingPeriod,
                selectedMonth = selection.month,
                selectedWeekStart = selection.weekStart,
                selectedYear = selection.year,
            ),
            mode,
            sortMode,
            accountingPeriod,
        )
    }.flatMapLatest { (category, range, mode, sortMode, _) ->
        category?.let {
            getStatsCategoryTransactionRankingUseCase.invoke(
                categoryId = it.categoryId,
                startDate = range.first(),
                endDate = range.last(),
                mode = mode,
                sortMode = sortMode,
            )
        } ?: emptyFlow()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _averageData = MutableStateFlow(
        WholeAverageData(
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            ),
            AverageData(
                "0.00$",
                "0.00$",
                "0.00$",
            ),
        ),
    )
    val averageData = _averageData.asStateFlow()

    init {
        getAverageDataUseCase.invoke().onEach { response ->
            _averageData.value = response
        }.launchIn(viewModelScope)

        getAmountStateUseCase.invoke().onEach { response ->
            _expenseFlowState.value = response
        }.launchIn(viewModelScope)

        getCurrentThemeUseCase.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)

        combine(
            getDateRangeUseCase.invoke(),
            observeAccountingPeriodUseCase?.invoke() ?: flowOf(null),
        ) { dateRange, accountingPeriod -> dateRange to accountingPeriod }.onEach { (dateRange, accountingPeriod) ->
            val pendingPeriod = pendingStatsPeriod
            if (pendingPeriod != null && !dateRange.matchesStatsPeriod(pendingPeriod, accountingPeriod)) {
                // Saving a selected range updates the stored dates and type in
                // separate DataStore transactions. Do not let the old value
                // briefly put the controls back on the previous period.
                return@onEach
            }
            pendingStatsPeriod = null

            when (dateRange.type) {
                DateRangeType.THIS_WEEK -> {
                    _selectedWeekStart.value = dateRange.dateRanges.toCalendarWeekStartOrNull()
                        ?: currentCalendarWeekStart()
                    _statsPeriod.value = StatsPeriod.WEEK
                }
                DateRangeType.THIS_MONTH -> {
                    if (accountingPeriod?.shouldUseCustomPeriod() == true) {
                        _statsPeriod.value = StatsPeriod.CURRENT_PERIOD
                    } else {
                        _selectedMonth.value = dateRange.dateRanges.toCalendarMonthOrNull()
                            ?: YearMonth.now()
                        _statsPeriod.value = StatsPeriod.MONTH
                    }
                }
                DateRangeType.THIS_YEAR -> {
                    _selectedYear.value = dateRange.dateRanges.toCalendarYearOrNull()
                        ?: LocalDate.now().year
                    _statsPeriod.value = StatsPeriod.YEAR
                }
                DateRangeType.TODAY,
                DateRangeType.ALL,
                    -> Unit
                DateRangeType.CUSTOM -> {
                    val month = dateRange.dateRanges.toCalendarMonthOrNull()
                    val weekStart = dateRange.dateRanges.toCalendarWeekStartOrNull()
                    val year = dateRange.dateRanges.toCalendarYearOrNull()
                    when {
                        month != null -> {
                            _selectedMonth.value = month
                            _statsPeriod.value = StatsPeriod.MONTH
                        }
                        weekStart != null -> {
                            _selectedWeekStart.value = weekStart
                            _statsPeriod.value = StatsPeriod.WEEK
                        }
                        year != null -> {
                            _selectedYear.value = year
                            _statsPeriod.value = StatsPeriod.YEAR
                        }
                    }
                }
            }
            _transactionPeriod.value = if (dateRange.description.isNotEmpty()) {
                "${dateRange.name} (${dateRange.description})"
            } else {
                dateRange.name
            }
        }.launchIn(viewModelScope)

        settingsRepository.getHomeSummaryCompact().onEach {
            _isCompactSummary.value = it
        }.launchIn(viewModelScope)
    }

    fun selectRankingMode(mode: RankingMode) {
        _rankingMode.value = mode
    }

    fun selectStatsPeriod(period: StatsPeriod) {
        pendingStatsPeriod = period
        _statsPeriod.value = period
        viewModelScope.launch {
            if (period == StatsPeriod.CURRENT_PERIOD) {
                val accountingPeriod = observeAccountingPeriodUseCase?.invoke()?.first()
                if (accountingPeriod != null) {
                    saveDateRangeUseCase.invoke(
                        DateRangeType.THIS_MONTH,
                        listOf(
                            Date(accountingPeriod.startDate.toEpochMillisAtStartOfDay()),
                            Date(accountingPeriod.endExclusiveDate.toEpochMillisAtStartOfDay()),
                        ),
                    )
                }
                return@launch
            }
            if (period == StatsPeriod.MONTH) {
                _selectedMonth.value = YearMonth.now()
                saveDateRangeUseCase.invoke(
                    DateRangeType.CUSTOM,
                    _selectedMonth.value.toDateRange().map(::Date),
                )
                return@launch
            }
            val dateRangeType = period.toDateRangeType()
            val ranges = when (val response = getAllDateRangeUseCase.invoke()) {
                is Resource.Success -> response.data
                    .firstOrNull { it.type == dateRangeType }
                    ?.dateRanges
                    ?.map(::Date)
                is Resource.Error -> null
            }
            if (ranges != null) {
                val rangeMillis = ranges.map(Date::getTime)
                when (period) {
                    StatsPeriod.WEEK -> {
                        _selectedWeekStart.value = rangeMillis.toCalendarWeekStartOrNull()
                            ?: currentCalendarWeekStart()
                    }
                    StatsPeriod.YEAR -> {
                        _selectedYear.value = rangeMillis.toCalendarYearOrNull()
                            ?: LocalDate.now().year
                    }
                    else -> Unit
                }
                saveDateRangeUseCase.invoke(dateRangeType, ranges)
            }
        }
    }

    /** Selects any calendar month and keeps every Stats query in that month in sync. */
    fun selectMonth(month: YearMonth) {
        pendingStatsPeriod = StatsPeriod.MONTH
        _selectedMonth.value = month
        _statsPeriod.value = StatsPeriod.MONTH
        viewModelScope.launch {
            saveDateRangeUseCase.invoke(
                DateRangeType.CUSTOM,
                month.toDateRange().map(::Date),
            )
        }
    }

    fun selectWeek(weekStart: LocalDate) {
        pendingStatsPeriod = StatsPeriod.WEEK
        val normalizedStart = weekStart.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
        _selectedWeekStart.value = normalizedStart
        _statsPeriod.value = StatsPeriod.WEEK
        viewModelScope.launch {
            saveDateRangeUseCase.invoke(
                DateRangeType.CUSTOM,
                normalizedStart.toWeekRange().map(::Date),
            )
        }
    }

    fun selectYear(year: Int) {
        if (year !in 1..9999) return
        pendingStatsPeriod = StatsPeriod.YEAR
        _selectedYear.value = year
        _statsPeriod.value = StatsPeriod.YEAR
        viewModelScope.launch {
            saveDateRangeUseCase.invoke(
                DateRangeType.CUSTOM,
                year.toDateRange().map(::Date),
            )
        }
    }

    fun openCategory(category: CategoryRankingItem) {
        _selectedCategory.value = category
        _detailSortMode.value = RankingSortMode.AMOUNT
    }

    fun closeCategory() {
        _selectedCategory.value = null
    }

    fun selectDetailSortMode(sortMode: RankingSortMode) {
        _detailSortMode.value = sortMode
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
)

enum class StatsPeriod {
    WEEK,
    MONTH,
    YEAR,
    CURRENT_PERIOD,
}

private fun StatsPeriod.toDateRangeType(): DateRangeType = when (this) {
    StatsPeriod.WEEK -> DateRangeType.THIS_WEEK
    StatsPeriod.MONTH -> DateRangeType.THIS_MONTH
    StatsPeriod.YEAR -> DateRangeType.THIS_YEAR
    StatsPeriod.CURRENT_PERIOD -> DateRangeType.THIS_MONTH
}

private fun DateRangeModel.matchesStatsPeriod(
    period: StatsPeriod,
    accountingPeriod: com.naveenapps.expensemanager.core.model.AccountingPeriod?,
): Boolean = when (period) {
    StatsPeriod.WEEK -> type == DateRangeType.THIS_WEEK ||
        (type == DateRangeType.CUSTOM && dateRanges.toCalendarWeekStartOrNull() != null)
    StatsPeriod.MONTH -> (type == DateRangeType.CUSTOM && dateRanges.toCalendarMonthOrNull() != null) ||
        (type == DateRangeType.THIS_MONTH && accountingPeriod?.shouldUseCustomPeriod() != true)
    StatsPeriod.YEAR -> type == DateRangeType.THIS_YEAR ||
        (type == DateRangeType.CUSTOM && dateRanges.toCalendarYearOrNull() != null)
    StatsPeriod.CURRENT_PERIOD -> type == DateRangeType.THIS_MONTH
}

private fun com.naveenapps.expensemanager.core.model.AccountingPeriod.shouldUseCustomPeriod(): Boolean =
    hasDefinedStart && mode != com.naveenapps.expensemanager.core.model.AccountingPeriodMode.CALENDAR_MONTH
