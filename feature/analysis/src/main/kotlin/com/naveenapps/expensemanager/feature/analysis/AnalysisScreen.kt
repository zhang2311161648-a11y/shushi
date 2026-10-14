package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalysisScreen(
    onRecordTransaction: () -> Unit = {},
    viewModel: AnalysisScreenViewModel = koinViewModel(),
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    AnalysisScreenScaffoldView(
        selectedCategory = selectedCategory,
        onBack = viewModel::closeCategory,
        onRecordTransaction = onRecordTransaction,
        viewModel = viewModel,
    )
}

@Composable
private fun AnalysisScreenScaffoldView(
    selectedCategory: com.naveenapps.expensemanager.core.model.CategoryRankingItem?,
    onBack: () -> Unit,
    onRecordTransaction: () -> Unit,
    viewModel: AnalysisScreenViewModel,
) {
    val title = selectedCategory?.let { category ->
        category.categoryTitleResId?.let { stringResource(it) } ?: category.categoryName
    } ?: stringResource(R.string.analysis)

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = title,
                navigationIcon = if (selectedCategory != null) {
                    Icons.AutoMirrored.Filled.ArrowBack
                } else {
                    null
                },
                navigationBackClick = onBack,
            )
        },
    ) { innerPadding ->
        AnalysisScreenContent(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            viewModel = viewModel,
            onRecordTransaction = onRecordTransaction,
        )
    }
}

@Composable
private fun AnalysisScreenContent(
    modifier: Modifier = Modifier,
    viewModel: AnalysisScreenViewModel,
    onRecordTransaction: () -> Unit,
) {
    Column(modifier = modifier) {
        AnalysisGraphScreen(
            viewModel = viewModel,
            onRecordTransaction = onRecordTransaction,
        )
    }
}

@Preview
@Composable
fun AnalysisScreenPreview() {
    NaveenAppsPreviewTheme(padding = 0.dp) {
        AnalysisScreen()
    }
}
