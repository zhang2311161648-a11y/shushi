package com.naveenapps.expensemanager.feature.settings.smartcategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.CategoryRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionRuleRepository
import com.naveenapps.expensemanager.core.repository.CategorySuggestionSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartCategorySettingsViewModel(
    private val settingsRepository: CategorySuggestionSettingsRepository,
    private val ruleRepository: CategorySuggestionRuleRepository,
    categoryRepository: CategoryRepository,
    private val navigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(SmartCategorySettingsState())
    val state = _state.asStateFlow()

    init {
        combine(
            settingsRepository.isEnabled(),
            ruleRepository.observeUserRules(),
            categoryRepository.getCategories(),
        ) { enabled, rules, categories ->
            SmartCategorySettingsState(
                isEnabled = enabled,
                learnedRules = rules.map { rule ->
                    LearnedCategoryRule(
                        rule = rule,
                        category = categories.firstOrNull { it.id == rule.categoryId },
                    )
                },
            )
        }.onEach { newState -> _state.value = newState }.launchIn(viewModelScope)
    }

    fun processAction(action: SmartCategorySettingsAction) {
        when (action) {
            SmartCategorySettingsAction.ClosePage -> navigator.popBackStack()
            SmartCategorySettingsAction.ToggleEnabled -> {
                viewModelScope.launch {
                    val response = settingsRepository.setEnabled(!_state.value.isEnabled)
                    if (response is Resource.Success && response.data) {
                        _state.update { it.copy(isEnabled = !it.isEnabled) }
                    }
                }
            }

            is SmartCategorySettingsAction.DeleteRule -> {
                viewModelScope.launch { ruleRepository.deleteUserRule(action.id) }
            }

            SmartCategorySettingsAction.ShowClearDialog -> {
                _state.update { it.copy(showClearDialog = true) }
            }

            SmartCategorySettingsAction.DismissClearDialog -> {
                _state.update { it.copy(showClearDialog = false) }
            }

            SmartCategorySettingsAction.ClearRules -> {
                viewModelScope.launch {
                    ruleRepository.clearUserRules()
                    _state.update { it.copy(showClearDialog = false) }
                }
            }
        }
    }
}
