package com.naveenapps.expensemanager

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.naveenapps.expensemanager.core.designsystem.utils.shouldUseDarkTheme
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.ui.AppLockScreen
import com.naveenapps.expensemanager.ui.JiangJiBrandSplash
import com.naveenapps.expensemanager.ui.JiangJiTheme
import com.naveenapps.expensemanager.ui.MainScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.scope.Scope

internal class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    private val appComposeNavigator: AppComposeNavigator by inject()

    override val scope: Scope by activityScope()

    private val viewModel: MainViewModel by viewModel()

    private val activityComponentProvider: ActivityComponentProvider by scope.inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        com.naveenapps.expensemanager.core.data.repository.FullDataBackup.consumeRestoreMessage(this)?.let {
            android.widget.Toast.makeText(this,it,android.widget.Toast.LENGTH_LONG).show()
        }

        enableEdgeToEdge()

        activityComponentProvider.getBackupRepository()

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.onboardingStatus.collectLatest {
                    splashScreen.setKeepOnScreenCondition {
                        it == null
                    }
                }
            }
        }

        setContent {
            // Provided explicitly rather than relying on the automatic ViewTree lookup that
            // rememberLauncherForActivityResult() falls back to (LocalView.current walking up to
            // the decor view's ViewTreeActivityResultRegistryOwner). That lookup can race with
            // window attachment right after this Activity is (re)created — e.g. following an
            // in-app locale switch, a config change, or process restoration — occasionally
            // resolving to null and crashing deep inside the Home screen's NavHost destination
            // with "No ActivityResultRegistryOwner was provided via LocalActivityResultRegistryOwner".
            // Providing it here up front removes that race entirely.
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides this) {
                val currentTheme by viewModel.currentTheme.collectAsState()
                val currentColorPalette by viewModel.currentColorPalette.collectAsState()
                val customColorHex by viewModel.customColorHex.collectAsState()
                val customBackgroundImageUri by viewModel.customBackgroundImageUri.collectAsState()
                val onBoardingStatus by viewModel.onboardingStatus.collectAsState()
                val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
                val isAuthenticated by viewModel.isAuthenticated.collectAsState()
                val isDarkTheme = shouldUseDarkTheme(theme = currentTheme.mode)
                var showBrandSplash by remember { mutableStateOf(true) }
                LaunchedEffect(onBoardingStatus) {
                    if (onBoardingStatus != null) {
                        kotlinx.coroutines.delay(320)
                        showBrandSplash = false
                    }
                }

                if (onBoardingStatus != null) {
                    val showLock = onBoardingStatus == true && isAppLockEnabled && !isAuthenticated

                    if (showBrandSplash) {
                        JiangJiTheme(isDarkTheme = isDarkTheme, palette = currentColorPalette) {
                            JiangJiBrandSplash()
                        }
                    } else if (showLock) {
                        JiangJiTheme(isDarkTheme = isDarkTheme, palette = currentColorPalette) {
                            LaunchedEffect(Unit) { showBiometricPrompt() }
                            AppLockScreen(onUnlockClick = ::showBiometricPrompt)
                        }
                    } else {
                        MainScreen(
                            composeNavigator = appComposeNavigator,
                            componentProvider = activityComponentProvider,
                            isDarkTheme = isDarkTheme,
                            palette = currentColorPalette,
                            customColorHex = customColorHex,
                            customBackgroundImageUri = customBackgroundImageUri,
                            landingScreen = if (onBoardingStatus == true) {
                                ExpenseManagerScreens.Home
                            } else {
                                ExpenseManagerScreens.IntroScreen
                            }
                        )

                    }
                }
            }
        }
    }

    private fun showBiometricPrompt() {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticate = BiometricManager.from(this).canAuthenticate(authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            // No biometric or device credentials enrolled — bypass the lock
            viewModel.onAuthenticationSuccess()
            return
        }

        val prompt = BiometricPrompt(
            /* activity = */ this,
            /* executor = */ ContextCompat.getMainExecutor(this),
            /* callback = */ object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.onAuthenticationSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // User canceled or hardware error — lock screen stays visible for retry
                    Log.d("AppLock", "Auth error $errorCode: $errString")
                }

                override fun onAuthenticationFailed() {
                    // Biometric not recognised — lock screen stays visible for retry
                }
            },
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_lock_title))
            .setDescription(getString(R.string.app_lock_description))
            .setAllowedAuthenticators(authenticators)
            .build()

        prompt.authenticate(promptInfo)
    }

}
