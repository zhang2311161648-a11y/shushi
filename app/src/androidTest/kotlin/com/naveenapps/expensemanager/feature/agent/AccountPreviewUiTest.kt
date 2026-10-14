package com.naveenapps.expensemanager.feature.agent

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.naveenapps.expensemanager.core.model.AccountUiModel
import com.naveenapps.expensemanager.core.model.Amount
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.ui.AccountBalanceSection
import com.naveenapps.expensemanager.ui.JiangJiTheme
import java.io.File
import org.junit.Rule
import org.junit.Test

class AccountPreviewUiTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @Test fun threeAccountsAndBalancesAreVisibleWithoutScrolling() {
        val accounts = listOf("微信" to "¥1116.40", "支付宝" to "¥300.00", "现金" to "¥888.88").mapIndexed { index, pair ->
            AccountUiModel("test-$index", pair.first, StoredIcon("wallet", "#6F1D2C"), Amount(0.0,pair.second), 0)
        }
        rule.setContent { JiangJiTheme(isDarkTheme = false) {
            AccountBalanceSection(accounts, "¥2305.28", {})
        } }
        accounts.forEach { rule.onNodeWithText(it.name).assertIsDisplayed(); rule.onNodeWithText(it.amount.amountString!!).assertIsDisplayed() }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File("/sdcard/Android/media/${instrumentation.targetContext.packageName}/additional_test_output").apply { mkdirs() }
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            try { File(directory,"three-accounts.png").outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) } }
            finally { bitmap.recycle() }
        }
    }
}
