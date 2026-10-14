package com.naveenapps.expensemanager.feature.settings.di

import com.naveenapps.expensemanager.feature.settings.SettingsViewModel
import com.naveenapps.expensemanager.feature.settings.NavigationOrderViewModel
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsViewModel
import com.naveenapps.expensemanager.feature.settings.smartcategory.SmartCategorySettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val SettingsViewModelModule = module {
    viewModel {
        SettingsViewModel(
            getDefaultCurrencyUseCase = get(),
            getCurrencyUseCase = get(),
            getCurrentThemeUseCase = get(),
            getCurrentLocaleUseCase = get(),
            getAllAccountsUseCase = get(),
            getAllCategoryUseCase = get(),
            settingsRepository = get(),
            appComposeNavigator = get(),
            getColorPaletteUseCase = get(),
            saveColorPaletteUseCase = get(),
            getCustomColorHexUseCase = get(),
            saveCustomColorHexUseCase = get(),
            getCustomBackgroundImageUriUseCase = get(),
            saveCustomBackgroundImageUriUseCase = get(),
        )
    }
    viewModel {
        AdvancedSettingsViewModel(
            appComposeNavigator = get()
        )
    }
    viewModel {
        SmartCategorySettingsViewModel(
            settingsRepository = get(),
            ruleRepository = get(),
            categoryRepository = get(),
            navigator = get(),
        )
    }
    viewModel {
        NavigationOrderViewModel(
            navigationOrderRepository = get(),
            appComposeNavigator = get(),
        )
    }
}

