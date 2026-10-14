package com.naveenapps.expensemanager

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.settings.onboarding.GetOnboardingStatusUseCase
import com.naveenapps.expensemanager.core.domain.usecase.settings.theme.GetCurrentThemeUseCase
import com.naveenapps.expensemanager.core.model.Theme
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette
import com.naveenapps.expensemanager.core.domain.usecase.settings.palette.GetColorPaletteUseCase
import com.naveenapps.expensemanager.core.repository.SettingsRepository
import com.naveenapps.expensemanager.feature.theme.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class MainViewModel(
    getCurrentThemeUseCase: GetCurrentThemeUseCase,
    getOnboardingStatusUseCase: GetOnboardingStatusUseCase,
    settingsRepository: SettingsRepository,
    getColorPaletteUseCase: GetColorPaletteUseCase? = null,
    getCustomColorHexUseCase: com.naveenapps.expensemanager.core.domain.usecase.settings.palette.GetCustomColorHexUseCase? = null,
    getCustomBackgroundImageUriUseCase: com.naveenapps.expensemanager.core.domain.usecase.settings.palette.GetCustomBackgroundImageUriUseCase? = null,
) : ViewModel() {

    private val _currentTheme = MutableStateFlow(
        Theme(
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
            R.string.choose_theme,
        ),
    )
    val currentTheme = _currentTheme.asStateFlow()

    private val _currentColorPalette = MutableStateFlow(JiangJiColorPalette.BURGUNDY)
    val currentColorPalette = _currentColorPalette.asStateFlow()

    private val _customColorHex = MutableStateFlow<String?>(null)
    val customColorHex = _customColorHex.asStateFlow()

    private val _customBackgroundImageUri = MutableStateFlow<String?>(null)
    val customBackgroundImageUri = _customBackgroundImageUri.asStateFlow()

    private val _onboardingStatus = MutableStateFlow<Boolean?>(null)
    val onboardingStatus = _onboardingStatus.asStateFlow()

    private val _isAppLockEnabled = MutableStateFlow(false)
    val isAppLockEnabled = _isAppLockEnabled.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        getCurrentThemeUseCase.invoke().onEach {
            _currentTheme.value = it
        }.launchIn(viewModelScope)

        getColorPaletteUseCase?.invoke()?.onEach {
            _currentColorPalette.value = it
        }?.launchIn(viewModelScope)

        getCustomColorHexUseCase?.invoke()?.onEach {
            _customColorHex.value = it
        }?.launchIn(viewModelScope)

        getCustomBackgroundImageUriUseCase?.invoke()?.onEach {
            _customBackgroundImageUri.value = it
        }?.launchIn(viewModelScope)

        viewModelScope.launch {
            _onboardingStatus.value = getOnboardingStatusUseCase.invoke()
        }

        settingsRepository.isAppLockEnabled().onEach {
            _isAppLockEnabled.value = it
        }.launchIn(viewModelScope)
    }

    fun onAuthenticationSuccess() {
        _isAuthenticated.value = true
    }
}
