package com.naveenapps.expensemanager.core.domain.usecase.di

import com.naveenapps.expensemanager.core.domain.usecase.account.AccountUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.asset.AssetUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.accountingperiod.AccountingPeriodUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief.AnxietyReliefUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.budget.BudgetUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.category.CategoryUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.categorysuggestion.CategorySuggestionEngine
import com.naveenapps.expensemanager.core.domain.usecase.country.CountryUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.monthlysaving.MonthlySavingUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.ledger.LedgerUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.profile.ProfileUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.settings.SettingsUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.settings.filter.FilterUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.stats.StatsRankingUseCaseModule
import com.naveenapps.expensemanager.core.domain.usecase.transaction.TransactionUseCaseModule
import org.koin.dsl.module

val UseCaseModule = module {
    single { CategorySuggestionEngine() }
    includes(
        AccountUseCaseModule,
        AccountingPeriodUseCaseModule,
        AssetUseCaseModule,
        AnxietyReliefUseCaseModule,
        BudgetUseCaseModule,
        CategoryUseCaseModule,
        CountryUseCaseModule,
        SettingsUseCaseModule,
        TransactionUseCaseModule,
        FilterUseCaseModule,
        StatsRankingUseCaseModule,
        MonthlySavingUseCaseModule,
        LedgerUseCaseModule,
        ProfileUseCaseModule,
    )
}
