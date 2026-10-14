package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.navigation.ExpenseManagerScreens
import com.naveenapps.expensemanager.core.repository.ActivityComponentProvider
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette

@Composable
fun MainScreen(
    composeNavigator: AppComposeNavigator,
    componentProvider: ActivityComponentProvider,
    isDarkTheme: Boolean,
    palette: JiangJiColorPalette = JiangJiColorPalette.BURGUNDY,
    customColorHex: String? = null,
    customBackgroundImageUri: String? = null,
    landingScreen: ExpenseManagerScreens,
) {
    JiangJiTheme(
        isDarkTheme = isDarkTheme,
        palette = palette,
        customColorHex = customColorHex,
        hasBackgroundImage = !customBackgroundImageUri.isNullOrBlank(),
    ) {
        Box(modifier = Modifier.fillMaxSize().background(JiangJiColors.Ivory)) {
            if (!customBackgroundImageUri.isNullOrBlank()) {
                AsyncImage(
                    model = customBackgroundImageUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.32f),
                )
            }
            val navHostController = rememberNavController()

            LaunchedEffect(Unit) {
                composeNavigator.handleNavigationCommands(navHostController)
            }

            HomePageNavHostContainer(
                componentProvider,
                navHostController,
                landingScreen,
                composeNavigator,
            )
        }
    }
}
