package com.naveenapps.expensemanager.feature.settings.smartcategory

import androidx.compose.runtime.Stable
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule

@Stable
data class SmartCategorySettingsState(
    val isEnabled: Boolean = true,
    val learnedRules: List<LearnedCategoryRule> = emptyList(),
    val showClearDialog: Boolean = false,
)

data class LearnedCategoryRule(
    val rule: CategorySuggestionRule,
    val category: Category?,
)
