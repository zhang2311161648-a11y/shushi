package com.naveenapps.expensemanager.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.naveenapps.expensemanager.R
import com.naveenapps.expensemanager.core.designsystem.utils.BackHandler
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.feature.about.AboutScreen
import com.naveenapps.expensemanager.feature.account.create.AccountCreateScreen
import com.naveenapps.expensemanager.feature.account.list.AccountListScreen
import com.naveenapps.expensemanager.feature.account.reorder.AccountReOrderScreen
import com.naveenapps.expensemanager.feature.analysis.AnalysisScreen
import com.naveenapps.expensemanager.feature.budget.create.BudgetCreateScreen
import com.naveenapps.expensemanager.feature.budget.details.BudgetDetailScreen
import com.naveenapps.expensemanager.feature.budget.list.BudgetListScreen
import com.naveenapps.expensemanager.feature.category.create.CategoryCreateScreen
import com.naveenapps.expensemanager.feature.category.details.CategoryDetailScreen
import com.naveenapps.expensemanager.feature.category.list.CategoryListScreen
import com.naveenapps.expensemanager.feature.category.transaction.CategoryTransactionTabScreen
import com.naveenapps.expensemanager.feature.currency.CurrencyCustomiseScreen
import com.naveenapps.expensemanager.feature.export.ExportScreen
import com.naveenapps.expensemanager.feature.onboarding.OnboardingScreen
import com.naveenapps.expensemanager.feature.onboarding.into.IntroScreen
import com.naveenapps.expensemanager.feature.reminder.ReminderScreen
import com.naveenapps.expensemanager.feature.settings.SettingsScreen
import com.naveenapps.expensemanager.feature.settings.NavigationOrderScreen
import com.naveenapps.expensemanager.feature.settings.smartcategory.SmartCategorySettingsScreen
import com.naveenapps.expensemanager.feature.settings.advanced.AdvancedSettingsScreen
import com.naveenapps.expensemanager.ui.JiangJiAccountingPeriodScreen
import com.naveenapps.expensemanager.feature.transaction.create.TransactionCreateScreen
import com.naveenapps.expensemanager.feature.transaction.list.TransactionListScreen
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch

@Composable
fun HomePageNavHostContainer(
    backupRepository: ActivityComponentProvider,
    navHostController: NavHostController,
    landingScreen: ExpenseManagerScreens,
    composeNavigator: AppComposeNavigator,
) {
    NavHost(
        navController = navHostController,
        startDestination = landingScreen,
    ) {
        this.expenseManagerNavigation(backupRepository, composeNavigator)
    }
}

fun NavGraphBuilder.expenseManagerNavigation(
    componentProvider: ActivityComponentProvider,
    composeNavigator: AppComposeNavigator,
) {
    composable<ExpenseManagerScreens.IntroScreen> {
        IntroScreen(componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.Onboarding> {
        OnboardingScreen()
    }
    composable<ExpenseManagerScreens.Home> {
        HomeScreen(
            composeNavigator = composeNavigator,
            componentProvider = componentProvider,
        )
    }
    composable<ExpenseManagerScreens.CategoryList> {
        CategoryListScreen()
    }
    composable<ExpenseManagerScreens.CategoryCreate> {
        CategoryCreateScreen()
    }
    composable<ExpenseManagerScreens.CategoryDetails> {
        CategoryDetailScreen()
    }
    composable<ExpenseManagerScreens.TransactionList> {
        TransactionListScreen(showBackNavigationIcon = true)
    }
    composable<ExpenseManagerScreens.TransactionCreate> {
        TransactionCreateScreen(shareRepository = componentProvider.getShareRepository())
    }
    composable<ExpenseManagerScreens.AccountList> {
        AccountListScreen()
    }
    composable<ExpenseManagerScreens.AccountCreate> {
        AccountCreateScreen()
    }
    composable<ExpenseManagerScreens.BudgetList> {
        BudgetListScreen()
    }
    composable<ExpenseManagerScreens.BudgetCreate> {
        BudgetCreateScreen()
    }
    composable<ExpenseManagerScreens.BudgetDetails> {
        BudgetDetailScreen()
    }
    composable<ExpenseManagerScreens.AnalysisScreen> {
        AnalysisScreen(
            onRecordTransaction = {
                composeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
            },
        )
    }
    composable<ExpenseManagerScreens.Settings> {
        SettingsScreen(
            shareRepository = componentProvider.getShareRepository(),
            backupRepository = componentProvider.getBackupRepository(),
        )
    }
    composable<ExpenseManagerScreens.ExportScreen> {
        FullBackupExportDialog(componentProvider.getBackupRepository()) { composeNavigator.popBackStack() }
    }
    composable<ExpenseManagerScreens.ReminderScreen> {
        ReminderScreen(
            shareRepository = componentProvider.getShareRepository()
        )
    }
    composable<ExpenseManagerScreens.CurrencyCustomiseScreen> {
        CurrencyCustomiseScreen()
    }
    composable<ExpenseManagerScreens.CategoryTransaction> {
        CategoryTransactionTabScreen()
    }
    composable<ExpenseManagerScreens.AboutUsScreen> {
        AboutScreen()
    }
    composable<ExpenseManagerScreens.AdvancedSettingsScreen> {
        AdvancedSettingsScreen()
    }
    composable<ExpenseManagerScreens.SmartCategorySettingsScreen> {
        SmartCategorySettingsScreen()
    }
    composable<ExpenseManagerScreens.AccountingPeriodSettingsScreen> {
        JiangJiAccountingPeriodScreen()
    }
    composable<ExpenseManagerScreens.AccountReOrderScreen> {
        AccountReOrderScreen()
    }
    composable<ExpenseManagerScreens.NavigationOrderSettingsScreen> {
        NavigationOrderScreen()
    }
    composable<ExpenseManagerScreens.LedgerSettingsScreen> {
        JiangJiLedgerScreen(
            onBack = { composeNavigator.popBackStack() },
        )
    }
}

@Composable
fun HomeScreen(
    composeNavigator: AppComposeNavigator,
    componentProvider: ActivityComponentProvider,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalActivity.current
    val appContext = LocalContext.current
    val selectedItem by viewModel.homeScreenBottomBarItems.collectAsState()
    val navigationOrder by viewModel.navigationOrder.collectAsState()
    val importScope = rememberCoroutineScope()

    BackHandler {
        if (selectedItem != HomeScreenBottomBarItems.Details) {
            viewModel.setUISystem(HomeScreenBottomBarItems.Details)
        } else {
            context?.finish()
        }
    }

    Scaffold(
        bottomBar = {
            JiangJiBottomBar(
                selectedItem = selectedItem,
                navigationOrder = navigationOrder,
                onItemSelected = viewModel::setUISystem,
                onAddClick = {
                    composeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (selectedItem) {
                HomeScreenBottomBarItems.Details -> {
                    JiangJiDetailsScreen(
                        onOpenTransaction = { transactionId ->
                            if (transactionId == null) {
                                composeNavigator.navigate(ExpenseManagerScreens.TransactionList)
                            } else {
                                composeNavigator.navigate(
                                    ExpenseManagerScreens.TransactionCreate(transactionId),
                                )
                            }
                        },
                        onAddTransaction = {
                            composeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
                        },
                        onOpenAccounts = {
                            composeNavigator.navigate(ExpenseManagerScreens.AccountList)
                        },
                        onOpenAccountingPeriod = {
                            composeNavigator.navigate(ExpenseManagerScreens.AccountingPeriodSettingsScreen)
                        },
                    )
                }

                HomeScreenBottomBarItems.Stats -> AnalysisScreen(
                    onRecordTransaction = {
                        composeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
                    },
                )
                HomeScreenBottomBarItems.Me -> JiangJiMeScreen(
                    onImportData = { uri ->
                        importScope.launch {
                            when (val result = componentProvider.getBackupRepository().importData(uri)) {
                                is Resource.Success -> {
                                    android.widget.Toast.makeText(
                                        appContext,
                                        appContext.getString(
                                            R.string.jiangji_import_success,
                                            result.data.importedCount,
                                            result.data.skippedCount,
                                        ),
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                }

                                is Resource.Error -> {
                                    android.widget.Toast.makeText(
                                        appContext,
                                        R.string.jiangji_import_failed,
                                        android.widget.Toast.LENGTH_LONG,
                                    ).show()
                                }
                            }
                        }
                    },
                    onSendFeedback = { message ->
                        componentProvider.getShareRepository().openFeedbackEmail(message)
                    },
                    onOpenLedger = {
                        composeNavigator.navigate(ExpenseManagerScreens.LedgerSettingsScreen)
                    },
                    onOpenRoute = { route ->
                        when (route) {
                            JiangJiMeRoute.Accounts -> composeNavigator.navigate(ExpenseManagerScreens.AccountList)
                            JiangJiMeRoute.Categories -> composeNavigator.navigate(ExpenseManagerScreens.CategoryList)
                            JiangJiMeRoute.Budgets -> composeNavigator.navigate(ExpenseManagerScreens.BudgetList)
                            JiangJiMeRoute.Export -> composeNavigator.navigate(ExpenseManagerScreens.ExportScreen)
                            JiangJiMeRoute.Settings -> composeNavigator.navigate(ExpenseManagerScreens.Settings)
                            JiangJiMeRoute.Currency -> composeNavigator.navigate(ExpenseManagerScreens.CurrencyCustomiseScreen)
                            JiangJiMeRoute.Reminder -> composeNavigator.navigate(ExpenseManagerScreens.ReminderScreen)
                            JiangJiMeRoute.About -> composeNavigator.navigate(ExpenseManagerScreens.AboutUsScreen)
                        }
                    },
                )

                HomeScreenBottomBarItems.Assets -> JiangJiAssetsScreen(
                    onNavigateToCreateTransaction = {
                        composeNavigator.navigate(ExpenseManagerScreens.TransactionCreate(null))
                    },
                )
            }
        }
    }
}

@Composable
private fun JiangJiBottomBar(
    selectedItem: HomeScreenBottomBarItems,
    navigationOrder: List<HomeScreenBottomBarItems>,
    onItemSelected: (HomeScreenBottomBarItems) -> Unit,
    onAddClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .background(MaterialTheme.colorScheme.background),
    ) {
        BottomAppBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(72.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 4.dp,
            contentPadding = PaddingValues(horizontal = 4.dp),
        ) {
            navigationOrder.take(2).forEach { item ->
                JiangJiNavigationItem(
                    item = item,
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.width(64.dp))
            navigationOrder.drop(2).forEach { item ->
                JiangJiNavigationItem(
                    item = item,
                    selected = selectedItem == item,
                    onClick = { onItemSelected(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(60.dp),
            shape = CircleShape,
            containerColor = JiangJiColors.Burgundy,
            contentColor = Color.White,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.jiangji_add_transaction),
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun JiangJiNavigationItem(
    item: HomeScreenBottomBarItems,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconOffset by animateDpAsState(
        targetValue = if (selected) (-2).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "navigationIconOffset",
    )
    val labelAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.72f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "navigationLabelAlpha",
    )

    Column(
        modifier = modifier
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = stringResource(item.labelResourceID),
            tint = if (selected) JiangJiColors.Burgundy else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .offset(y = iconOffset),
        )
        Text(
            text = stringResource(item.labelResourceID),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 12.sp,
                lineHeight = 14.sp,
            ),
            color = if (selected) JiangJiColors.Burgundy else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(labelAlpha),
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
