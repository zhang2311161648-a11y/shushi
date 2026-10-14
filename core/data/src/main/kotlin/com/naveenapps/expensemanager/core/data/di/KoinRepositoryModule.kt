package com.naveenapps.expensemanager.core.data.di

import com.naveenapps.expensemanager.core.data.repository.AccountRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CurrentLedgerRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.LedgerRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AssetRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AnxietyReliefRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AnxietyReliefSettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.MonthlySavingRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AnalyticsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.BudgetRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategoryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategorySuggestionRuleRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CategorySuggestionSettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ColorPaletteRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.AccountingPeriodRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CountryRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.CurrencyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DateRangeFilterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.DevicePropertyRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ExportRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ExchangeRateRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.export.CsvExportStrategy
import com.naveenapps.expensemanager.core.data.repository.export.PdfExportStrategy
import com.naveenapps.expensemanager.core.data.repository.FeedbackRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.LocalSettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ImageStorageRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ProfileRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.JsonConverterRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.LocaleRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.NavigationOrderRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ReminderTimeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.SettingsRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ShareRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.StatsRankingRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.ThemeRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.TransactionRepositoryImpl
import com.naveenapps.expensemanager.core.data.repository.VersionCheckerRepositoryImpl
import com.naveenapps.expensemanager.core.repository.AccountRepository
import com.naveenapps.expensemanager.core.repository.CurrentLedgerRepository
import com.naveenapps.expensemanager.core.repository.AssetRepository
import com.naveenapps.expensemanager.core.repository.AnxietyReliefRepository
import com.naveenapps.expensemanager.core.repository.AnxietyReliefSettingsRepository
import com.naveenapps.expensemanager.core.repository.MonthlySavingRepository
import com.naveenapps.expensemanager.core.repository.AnalyticsRepository
import com.naveenapps.expensemanager.core.repository.BudgetRepository
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionRuleRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionSettingsRepository
import com.naveenapps.expensemanager.core.repository.ColorPaletteRepository
import com.naveenapps.expensemanager.core.repository.AccountingPeriodRepository
import com.naveenapps.expensemanager.core.repository.CountryRepository
import com.naveenapps.expensemanager.core.repository.CurrencyRepository
import com.naveenapps.expensemanager.core.repository.DateRangeFilterRepository
import com.naveenapps.expensemanager.core.repository.DevicePropertyRepository
import com.naveenapps.expensemanager.core.repository.ExportRepository
import com.naveenapps.expensemanager.core.repository.ExchangeRateRepository
import com.naveenapps.expensemanager.core.repository.FeedbackRepository
import com.naveenapps.expensemanager.core.repository.AppSettingsRepository
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import com.naveenapps.expensemanager.core.repository.ProfileRepository
import com.naveenapps.expensemanager.core.repository.JsonConverterRepository
import com.naveenapps.expensemanager.core.repository.LocaleRepository
import com.naveenapps.expensemanager.core.repository.LedgerRepository
import com.naveenapps.expensemanager.core.repository.NavigationOrderRepository
import com.naveenapps.expensemanager.core.repository.ReminderTimeRepository
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import com.naveenapps.expensemanager.core.repository.StatsRankingRepository
import com.naveenapps.expensemanager.core.repository.ThemeRepository
import com.naveenapps.expensemanager.core.repository.TransactionRepository
import com.naveenapps.expensemanager.core.repository.VersionCheckerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val RepositoryModule = module {
    single<ColorPaletteRepository> {
        ColorPaletteRepositoryImpl(
            dataStore = get(),
            dispatchers = get(),
        )
    }
    single<ThemeRepository> {
        ThemeRepositoryImpl(
            dataStore = get(),
            versionCheckerRepository = get(),
            dispatchers = get()
        )
    }
    single<LocaleRepository> {
        LocaleRepositoryImpl(
            dataStore = get(),
            dispatchers = get()
        )
    }
    single<AnalyticsRepository> {
        AnalyticsRepositoryImpl()
    }
    single<JsonConverterRepository> {
        JsonConverterRepositoryImpl(
            gson = get(),
            appCoroutineDispatchers = get()
        )
    }
    single<CountryRepository> {
        CountryRepositoryImpl(
            context = androidContext(),
            jsonConverterRepository = get(),
            dispatchers = get()
        )
    }
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            dispatchers = get(),
            dataStore = get(),
            numberFormatRepository = get(),
        )
    }
    single<DevicePropertyRepository> { DevicePropertyRepositoryImpl(androidContext()) }
    single<FeedbackRepository> {
        FeedbackRepositoryImpl(
            context = androidContext(),
            feedbackDataStore = get(),
        )
    }
    single<AppSettingsRepository> { LocalSettingsRepositoryImpl() }
    single<ImageStorageRepository> { ImageStorageRepositoryImpl(context = androidContext()) }
    single<ProfileRepository> {
        ProfileRepositoryImpl(
            dataStore = get(),
            dispatchers = get(),
        )
    }
    single<VersionCheckerRepository> { VersionCheckerRepositoryImpl() }

    single<CurrentLedgerRepository> {
        CurrentLedgerRepositoryImpl(
            dataStore = get(),
            dispatchers = get(),
        )
    }
    single<LedgerRepository> {
        LedgerRepositoryImpl(
            ledgerDao = get(),
            categoryDao = get(),
            accountDao = get(),
            database = get(),
            currentLedgerRepository = get(),
            dispatchers = get(),
        )
    }

    single<AccountRepository> {
        AccountRepositoryImpl(
            accountDao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<ExchangeRateRepository> {
        ExchangeRateRepositoryImpl(
            exchangeRateDataStore = get(),
            dispatchers = get(),
        )
    }
    single<NavigationOrderRepository> {
        NavigationOrderRepositoryImpl(settingsDataStore = get())
    }
    single<AssetRepository> {
        AssetRepositoryImpl(
            assetDao = get(),
            dispatchers = get(),
        )
    }
    single<AnxietyReliefRepository> {
        AnxietyReliefRepositoryImpl(
            itemDao = get(),
            dispatchers = get(),
        )
    }
    single<AnxietyReliefSettingsRepository> { AnxietyReliefSettingsRepositoryImpl(get()) }
    single<MonthlySavingRepository> {
        MonthlySavingRepositoryImpl(
            monthlySavingDao = get(),
            dispatchers = get(),
        )
    }
    single<BudgetRepository> {
        BudgetRepositoryImpl(
            budgetDao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<DateRangeFilterRepository> {
        DateRangeFilterRepositoryImpl(
            context = androidContext(),
            dataStore = get(),
            dispatcher = get()
        )
    }
    single<ExportRepository> {
        ExportRepositoryImpl(
            dispatchers = get(),
            strategies = listOf(
                CsvExportStrategy(context = androidContext()),
                PdfExportStrategy(context = androidContext()),
            )
        )
    }
    single<ReminderTimeRepository> {
        ReminderTimeRepositoryImpl(
            dataStore = get(),
            dispatchers = get()
        )
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            dataStore = get(),
            dispatchers = get()
        )
    }
    single<ShareRepository> {
        ShareRepositoryImpl(
            context = androidContext(),
            appSettingsRepository = get()
        )
    }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            accountDao = get(),
            categoryDao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<StatsRankingRepository> {
        StatsRankingRepositoryImpl(
            statsRankingDao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<CategoryRepository> {
        CategoryRepositoryImpl(
            categoryDao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<CategorySuggestionRuleRepository> {
        CategorySuggestionRuleRepositoryImpl(
            dao = get(),
            dispatchers = get(),
            currentLedgerRepository = get(),
        )
    }
    single<CategorySuggestionSettingsRepository> {
        CategorySuggestionSettingsRepositoryImpl(
            dataStore = get(),
            dispatchers = get(),
        )
    }
    single<AccountingPeriodRepository> {
        AccountingPeriodRepositoryImpl(
            settingsDataStore = get(),
            salaryCycleDao = get(),
            dispatchers = get(),
        )
    }
}
