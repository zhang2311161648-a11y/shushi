package com.naveenapps.expensemanager.di

import com.naveenapps.expensemanager.MainViewModel
import com.naveenapps.expensemanager.core.designsystem.components.CommonViewModelModule
import com.naveenapps.expensemanager.core.settings.di.CoreSettingsModule
import com.naveenapps.expensemanager.feature.about.di.AboutViewModelModule
import com.naveenapps.expensemanager.feature.account.di.AccountViewModelModule
import com.naveenapps.expensemanager.feature.analysis.di.AnalysisViewModelModule
import com.naveenapps.expensemanager.feature.budget.di.BudgetViewModelModule
import com.naveenapps.expensemanager.feature.category.di.CategoryViewModelModule
import com.naveenapps.expensemanager.feature.country.di.CountryViewModelModule
import com.naveenapps.expensemanager.feature.currency.di.CurrencyViewModelModule
import com.naveenapps.expensemanager.feature.dashboard.di.DashboardViewModelModule
import com.naveenapps.expensemanager.feature.export.di.ExportViewModelModule
import com.naveenapps.expensemanager.feature.filter.di.FilterViewModelModule
import com.naveenapps.expensemanager.feature.language.di.LanguageViewModelModule
import com.naveenapps.expensemanager.feature.onboarding.di.OnboardingViewModelModule
import com.naveenapps.expensemanager.feature.reminder.di.ReminderViewModelModule
import com.naveenapps.expensemanager.feature.settings.di.SettingsViewModelModule
import com.naveenapps.expensemanager.feature.theme.di.ThemeViewModelModule
import com.naveenapps.expensemanager.feature.transaction.di.TransactionViewModelModule
import com.naveenapps.expensemanager.ui.HomeViewModel
import com.naveenapps.expensemanager.ui.JiangJiAnxietyReliefViewModel
import com.naveenapps.expensemanager.ui.JiangJiAccountingPeriodViewModel
import com.naveenapps.expensemanager.ui.JiangJiSavingsViewModel
import com.naveenapps.expensemanager.ui.JiangJiLedgerViewModel
import com.naveenapps.expensemanager.ui.JiangJiProfileViewModel
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ArchiveLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.CreateLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ObserveCurrentLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.ObserveLedgersUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.RenameLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.ledger.SwitchLedgerUseCase
import com.naveenapps.expensemanager.core.domain.usecase.profile.ObserveUserProfileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.profile.SaveUserProfileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.DeleteMonthlySavingUseCase
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.GetMonthlySavingsYearTotalUseCase
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.ObserveMonthlySavingUseCase
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.ObserveMonthlySavingsByYearUseCase
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.UpsertMonthlySavingUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val MainViewModelModule = module {
    viewModel {
        MainViewModel(
            getCurrentThemeUseCase = get(),
            getOnboardingStatusUseCase = get(),
            settingsRepository = get(),
            getColorPaletteUseCase = get(),
            getCustomColorHexUseCase = get(),
            getCustomBackgroundImageUriUseCase = get(),
        )
    }
    viewModel {
        HomeViewModel(
            navigationOrderRepository = get(),
        )
    }
    viewModel {
        JiangJiAnxietyReliefViewModel(
            getItemsUseCase = get(),
            getDefaultTargetUseCase = get(),
            getCurrencyUseCase = get(),
            addItemUseCase = get(),
            updateItemUseCase = get(),
            archiveItemUseCase = get(),
            deleteItemUseCase = get(),
            setDefaultTargetUseCase = get(),
        )
    }
    viewModel {
        JiangJiAccountingPeriodViewModel(
            repository = get(),
            observeAccountingPeriod = get(),
            navigator = get(),
        )
    }
    viewModel {
        JiangJiSavingsViewModel(
            savingsPlanDataStore = get(),
            transactionRepository = get(),
            getCurrencyUseCase = get(),
            monthlySavingDao = get(),
        )
    }
    viewModel {
        JiangJiLedgerViewModel(
            observeLedgers = get<ObserveLedgersUseCase>(),
            observeCurrentLedger = get<ObserveCurrentLedgerUseCase>(),
            createLedger = get<CreateLedgerUseCase>(),
            renameLedger = get<RenameLedgerUseCase>(),
            switchLedger = get<SwitchLedgerUseCase>(),
            archiveLedger = get<ArchiveLedgerUseCase>(),
        )
    }
    viewModel {
        JiangJiProfileViewModel(
            observeUserProfile = get<ObserveUserProfileUseCase>(),
            saveUserProfile = get<SaveUserProfileUseCase>(),
            imageStorageRepository = get(),
        )
    }
}

val ViewModelModule = module {
    includes(
        MainViewModelModule,
        AccountViewModelModule,
        AnalysisViewModelModule,
        BudgetViewModelModule,
        CategoryViewModelModule,
        DashboardViewModelModule,
        TransactionViewModelModule,
        OnboardingViewModelModule,
        SettingsViewModelModule,
        ThemeViewModelModule,
        LanguageViewModelModule,
        ExportViewModelModule,
        ReminderViewModelModule,
        CurrencyViewModelModule,
        AboutViewModelModule,
        FilterViewModelModule,
        CountryViewModelModule,
        CommonViewModelModule,
        CoreSettingsModule,
    )
}
