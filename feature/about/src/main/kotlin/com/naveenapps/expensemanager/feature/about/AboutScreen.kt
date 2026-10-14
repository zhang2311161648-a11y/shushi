package com.naveenapps.expensemanager.feature.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.designsystem.utils.AppPreviewsLightAndDarkMode
import com.naveenapps.expensemanager.core.common.utils.openWebPage
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import com.naveenapps.expensemanager.core.designsystem.ui.components.SettingRow

@Composable
fun AboutScreen(
    viewModel: AboutUsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val context = LocalContext.current

    AboutUsScreenScaffoldView {
        when (it) {
            AboutUsOption.ACKNOWLEDGEMENTS -> {
                context.openWebPage(OPEN_SOURCE_REPOSITORY_URL)
            }

            AboutUsOption.BACK -> {
                viewModel.processAction(AboutAction.ClosePage)
            }
        }
    }
}

@Composable
private fun AboutUsScreenScaffoldView(
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(id = R.string.about_us),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = {
                    settingOptionSelected?.invoke(AboutUsOption.BACK)
                }
            )
        },
    ) { innerPadding ->
        AboutUsScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding),
            settingOptionSelected = settingOptionSelected
        )
    }
}

@Composable
private fun AboutUsScreenContent(
    modifier: Modifier = Modifier,
    settingOptionSelected: ((AboutUsOption) -> Unit)? = null,
) {
    Column(modifier = modifier) {
        AppCardView(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        ) {
            SettingRow(
                onClick = {},
                title = stringResource(id = R.string.about_developer_label),
                subtitle = stringResource(id = R.string.about_developer_name),
                icon = Icons.Outlined.Info,
                showDivider = true,
                showChevron = false,
            )
            SettingRow(
                onClick = {
                    settingOptionSelected?.invoke(AboutUsOption.ACKNOWLEDGEMENTS)
                },
                title = stringResource(id = R.string.about_acknowledgements),
                subtitle = stringResource(id = R.string.about_open_source_repository),
                icon = Icons.Outlined.FolderOpen,
            )
        }
    }
}

private enum class AboutUsOption {
    BACK,
    ACKNOWLEDGEMENTS,
}

private const val OPEN_SOURCE_REPOSITORY_URL = "https://github.com/nkuppan/expensemanager.git"

@Composable
@AppPreviewsLightAndDarkMode
fun AboutUsScreenScaffoldViewPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AboutUsScreenScaffoldView { }
    }
}
