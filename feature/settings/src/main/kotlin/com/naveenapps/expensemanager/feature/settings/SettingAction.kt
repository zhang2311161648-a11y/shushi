package com.naveenapps.expensemanager.feature.settings

import com.naveenapps.expensemanager.core.model.Account
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette

sealed class SettingAction {

    data object ClosePage : SettingAction()

    data object OpenExport : SettingAction()

    data object OpenRateUs : SettingAction()

    data object OpenAdvancedSettings : SettingAction()

    data object OpenSmartCategorySettings : SettingAction()

    data object OpenAboutUs : SettingAction()

    data object OpenNotification : SettingAction()

    data object OpenCurrencyEdit : SettingAction()

    data object OpenNavigationOrder : SettingAction()

    data object ShowThemeSelection : SettingAction()

    data object DismissThemeSelection : SettingAction()

    data object ShowLanguageSelection : SettingAction()

    data object DismissLanguageSelection : SettingAction()

    data object ShowPaletteSelection : SettingAction()

    data object DismissPaletteSelection : SettingAction()

    data class SelectPalette(val palette: JiangJiColorPalette) : SettingAction()
    data class SelectCustomColor(val hex: String) : SettingAction()
    data class SelectCustomBackgroundImage(val uri: String?) : SettingAction()

    // Defaults section (moved here from AdvancedSettingAction)
    data class SelectAccount(val account: Account) : SettingAction()

    data class SelectExpenseCategory(val category: Category) : SettingAction()

    data class SelectIncomeCategory(val category: Category) : SettingAction()

    data object ToggleCompactSummary : SettingAction()

    // Data & Backup section
    data object Backup : SettingAction()

    data object Restore : SettingAction()

    data object ImportData : SettingAction()

    // Security section
    data object ToggleAppLock : SettingAction()
}
