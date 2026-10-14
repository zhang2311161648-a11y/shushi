package com.naveenapps.expensemanager.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File

class AnxietyAssetOverviewUiTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    @Test fun gridSearchStatusAndUsageWork() {
        val sample = listOf(
            AnxietyReliefItem("car", "每日通勤 · 汽车", null, 13500000, "2023-08-12", 10000, "", 4, 4, category = "每日通勤"),
            AnxietyReliefItem("mac", "MacBook Air 15 寸", null, 1099900, "2024-01-02", 1500, "", 3, 3, category = "数码设备"),
            AnxietyReliefItem("phone", "iPhone 14 Pro", null, 879900, "2024-01-17", 1000, "", 2, 2, category = "数码设备", isArchived = true),
            AnxietyReliefItem("camera", "富士 X-S10 相机", null, 749900, "2024-01-02", 10000, "", 1, 1, category = "摄影", costByUse = true, useCount = 8),
        )
        val state = mutableStateOf(JiangJiAnxietyReliefState(items = sample))
        var uses = 0
        rule.setContent { JiangJiTheme(isDarkTheme = false) {
            AnxietyAssetOverview(state.value, {}, {}, { item ->
                uses++
                state.value = state.value.copy(items = state.value.items.map { if (it.id == item.id) it.copy(useCount = it.useCount + 1) else it })
            }, onEdit = {}, onDelete = {})
        } }
        rule.onNodeWithText("资产总览").assertIsDisplayed()
        rule.onNodeWithText("MacBook Air 15 寸").assertIsDisplayed()
        screenshot("anxiety-overview.png")
        rule.onAllNodesWithText("已归档")[0].performClick()
        rule.onNodeWithText("iPhone 14 Pro").assertIsDisplayed()
        rule.onNodeWithText("MacBook Air 15 寸").assertDoesNotExist()
        rule.onAllNodesWithText("全部")[1].performClick()
        rule.onNodeWithContentDescription("搜索物品").performClick()
        rule.onNodeWithText("搜索名称或备注").performTextInput("相机")
        rule.onNodeWithText("富士 X-S10 相机").assertIsDisplayed()
        rule.onNodeWithText("MacBook Air 15 寸").assertDoesNotExist()
        rule.onNodeWithContentDescription("富士 X-S10 相机使用一次").performClick()
        rule.runOnIdle { assertEquals(1, uses); assertEquals(9L, state.value.items.last().useCount) }
    }
    @Test fun everyItemHasEditAndDeleteAndSoldFilterIsGone() {
        val sample = AnxietyReliefItem("test", "测试物品", null, 1000, "2026-10-01", 500, "", 1, 1)
        val state = mutableStateOf(JiangJiAnxietyReliefState(items = listOf(sample)))
        var edited: String? = null
        var deleted: String? = null
        rule.setContent { JiangJiTheme(isDarkTheme = false) {
            AnxietyAssetOverview(state.value, {}, {}, {}, onEdit = { edited = it.id }, onDelete = { deleted = it.id })
        } }
        listOf(sample, sample.copy(isArchived = true), sample.copy(isSold = true)).forEach { item ->
            rule.runOnIdle { state.value = state.value.copy(items = listOf(item)) }
            rule.onAllNodesWithText("已卖出").assertCountEquals(0)
            rule.onNodeWithContentDescription("测试物品更多操作").performScrollTo().performClick()
            rule.onNodeWithText("编辑").performClick()
            rule.runOnIdle { assertEquals(item.id, edited); edited = null }
            rule.onNodeWithContentDescription("测试物品更多操作").performClick()
            rule.onNodeWithText("删除").performClick()
            rule.runOnIdle { assertEquals(item.id, deleted); deleted = null }
        }
        screenshot("anxiety-edit-delete.png")
    }

    @Test fun deletingRequiresConfirmationAndCancelKeepsTheItem() {
        val sample = AnxietyReliefItem("test", "测试物品", null, 1000, "2026-10-01", 500, "", 1, 1)
        val showDialog = mutableStateOf(true)
        var deleted = false
        rule.setContent { JiangJiTheme(isDarkTheme = false) {
            if (showDialog.value) AnxietyDeleteDialog(sample, false, null,
                onDismiss = { showDialog.value = false }, onConfirm = { deleted = true; showDialog.value = false })
        } }
        rule.onNodeWithText("取消").performClick()
        rule.runOnIdle { assertTrue(!deleted); showDialog.value = true }
        rule.onNodeWithText("删除").performClick()
        rule.runOnIdle { assertTrue(deleted) }
    }
    private fun screenshot(name: String) {
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val folder = File("/sdcard/Android/media/${instrumentation.targetContext.packageName}/additional_test_output").apply { mkdirs() }
        rule.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            var darkPixels = 0
            for (y in 0 until bitmap.height step 8) for (x in 0 until bitmap.width step 8) {
                val pixel = bitmap.getPixel(x, y)
                if (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel) < 150) darkPixels++
            }
            assertTrue("Overview screenshot must contain the rendered black summary", darkPixels > 100)
            try { File(folder, name).outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) } }
            finally { bitmap.recycle() }
        }
    }
}
