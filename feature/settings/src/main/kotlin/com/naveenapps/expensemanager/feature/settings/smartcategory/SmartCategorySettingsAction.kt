package com.naveenapps.expensemanager.feature.settings.smartcategory

sealed class SmartCategorySettingsAction {
    data object ClosePage : SmartCategorySettingsAction()
    data object ToggleEnabled : SmartCategorySettingsAction()
    data class DeleteRule(val id: Long) : SmartCategorySettingsAction()
    data object ShowClearDialog : SmartCategorySettingsAction()
    data object DismissClearDialog : SmartCategorySettingsAction()
    data object ClearRules : SmartCategorySettingsAction()
}
