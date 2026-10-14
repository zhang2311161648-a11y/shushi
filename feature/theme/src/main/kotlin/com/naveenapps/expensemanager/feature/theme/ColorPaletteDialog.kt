package com.naveenapps.expensemanager.feature.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.model.JiangJiColorPalette

private data class PaletteOption(
    val palette: JiangJiColorPalette,
    val primary: Color,
    val background: Color,
    val accent: Color,
)

private val paletteOptions = listOf(
    PaletteOption(
        JiangJiColorPalette.BURGUNDY,
        primary = Color(0xFF6F1D2C),
        background = Color(0xFFF7F0E5),
        accent = Color(0xFFB89A68),
    ),
    PaletteOption(
        JiangJiColorPalette.MORANDI_SAGE,
        primary = Color(0xFF53665B),
        background = Color(0xFFF1F2ED),
        accent = Color(0xFFA4936A),
    ),
    PaletteOption(
        JiangJiColorPalette.LAVENDER_MIST,
        primary = Color(0xFF756A82),
        background = Color(0xFFF2EEF5),
        accent = Color(0xFFA89472),
    ),
    PaletteOption(
        JiangJiColorPalette.MACARON_MIST,
        primary = Color(0xFF96737A),
        background = Color(0xFFF8F1ED),
        accent = Color(0xFFB69A78),
    ),
    PaletteOption(
        JiangJiColorPalette.OAT_CREAM,
        primary = Color(0xFF8A6652),
        background = Color(0xFFF5F0E8),
        accent = Color(0xFFA68D67),
    ),
    PaletteOption(
        JiangJiColorPalette.DUSTY_ROSE,
        primary = Color(0xFF925C67),
        background = Color(0xFFF6EFEE),
        accent = Color(0xFFA88D75),
    ),
    PaletteOption(
        JiangJiColorPalette.MISTY_SLATE,
        primary = Color(0xFF516879),
        background = Color(0xFFEEF1F3),
        accent = Color(0xFFA18F70),
    ),
)

@Composable
fun ColorPaletteDialogView(
    selectedPalette: JiangJiColorPalette,
    customColorHex: String? = null,
    customBgUri: String? = null,
    onSelect: (JiangJiColorPalette) -> Unit,
    onCustomColorSelected: (String) -> Unit = {},
    onCustomBackgroundSelected: (String?) -> Unit = {},
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AppCardView(modifier = Modifier.padding(16.dp)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.choose_color_palette),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(R.string.choose_color_palette_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(paletteOptions, key = { it.palette.id }) { option ->
                    val isSelected = option.palette == selectedPalette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(option.palette) }
                            .then(
                                if (isSelected) {
                                    Modifier.background(
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                } else {
                                    Modifier
                                },
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PaletteSwatches(option)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = colorPaletteTitle(option.palette),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                            Text(
                                text = colorPaletteDescription(option.palette),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.78f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(10.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "自定义调色盘",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "选择色盘颜色打造独一无二的专属主题",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        FreeColorPicker(customColorHex, onCustomColorSelected)
                    }
                }
                item {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val scope = rememberCoroutineScope()
                    var isSaving by remember { mutableStateOf(false) }
                    var imageError by remember { mutableStateOf<String?>(null) }
                    val bgPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
                    ) { uri ->
                        uri?.let { source ->
                            scope.launch {
                                isSaving = true
                                imageError = null
                                val result = withContext(Dispatchers.IO) {
                                    runCatching {
                                        val directory = java.io.File(context.filesDir, "appearance").apply { mkdirs() }
                                        val file = java.io.File(directory, "background-${java.util.UUID.randomUUID()}")
                                        try {
                                            context.contentResolver.openInputStream(source)?.use { input ->
                                                file.outputStream().use { output -> input.copyTo(output) }
                                            } ?: error("无法读取照片")
                                            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                                            android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
                                            check(bounds.outWidth > 0 && bounds.outHeight > 0) { "照片格式不受支持" }
                                            android.net.Uri.fromFile(file).toString()
                                        } catch (error: Exception) {
                                            file.delete()
                                            throw error
                                        }
                                    }
                                }
                                result.onSuccess(onCustomBackgroundSelected).onFailure {
                                    imageError = "照片保存失败，请重新选择"
                                }
                                isSaving = false
                            }
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "自定义背景照片",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "挑选喜欢的照片作为软件氛围背景图",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        imageError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            androidx.compose.material3.Button(
                                enabled = !isSaving,
                                onClick = { bgPickerLauncher.launch(arrayOf("image/*")) },
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(if (isSaving) "保存中…" else "选择本地照片")
                            }
                            if (!customBgUri.isNullOrBlank()) {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = { onCustomBackgroundSelected(null) },
                                ) {
                                    Text("清除背景")
                                }
                            }
                        }
                    }
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.CenterEnd) {
                        androidx.compose.material3.TextButton(onClick = onDismiss) {
                            Text("完成")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun colorPaletteTitle(palette: JiangJiColorPalette): String = stringResource(
    id = when (palette) {
        JiangJiColorPalette.BURGUNDY -> R.string.palette_burgundy
        JiangJiColorPalette.MORANDI_SAGE -> R.string.palette_morandi_sage
        JiangJiColorPalette.LAVENDER_MIST -> R.string.palette_lavender_mist
        JiangJiColorPalette.MACARON_MIST -> R.string.palette_macaron_mist
        JiangJiColorPalette.OAT_CREAM -> R.string.palette_oat_cream
        JiangJiColorPalette.DUSTY_ROSE -> R.string.palette_dusty_rose
        JiangJiColorPalette.MISTY_SLATE -> R.string.palette_misty_slate
        JiangJiColorPalette.CUSTOM -> R.string.palette_custom
    },
)

@Composable
private fun colorPaletteDescription(palette: JiangJiColorPalette): String = stringResource(
    id = when (palette) {
        JiangJiColorPalette.BURGUNDY -> R.string.palette_burgundy_description
        JiangJiColorPalette.MORANDI_SAGE -> R.string.palette_morandi_sage_description
        JiangJiColorPalette.LAVENDER_MIST -> R.string.palette_lavender_mist_description
        JiangJiColorPalette.MACARON_MIST -> R.string.palette_macaron_mist_description
        JiangJiColorPalette.OAT_CREAM -> R.string.palette_oat_cream_description
        JiangJiColorPalette.DUSTY_ROSE -> R.string.palette_dusty_rose_description
        JiangJiColorPalette.MISTY_SLATE -> R.string.palette_misty_slate_description
        JiangJiColorPalette.CUSTOM -> R.string.palette_custom_description
    },
)

@Composable
private fun PaletteSwatches(option: PaletteOption) {
    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
        listOf(option.background, option.accent, option.primary).forEach { color ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color = color, shape = CircleShape),
            )
        }
    }
}

@Composable
private fun FreeColorPicker(initialHex: String?, onApply: (String) -> Unit) {
    var hex by remember(initialHex) { mutableStateOf(initialHex ?: "#6F1D2C") }
    val initialHsv = remember(initialHex) {
        FloatArray(3).also {
            android.graphics.Color.colorToHSV(
                runCatching { android.graphics.Color.parseColor(initialHex ?: "#6F1D2C") }
                    .getOrDefault(android.graphics.Color.rgb(111, 29, 44)), it,
            )
        }
    }
    var hue by remember(initialHex) { mutableStateOf(initialHsv[0]) }
    var saturation by remember(initialHex) { mutableStateOf(initialHsv[1]) }
    var brightness by remember(initialHex) { mutableStateOf(initialHsv[2]) }
    fun updateHex() {
        hex = String.format("#%06X", android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness)) and 0xFFFFFF)
    }
    val valid = hex.matches(Regex("#[0-9A-Fa-f]{6}"))
    val preview = if (valid) Color(android.graphics.Color.parseColor(hex)) else MaterialTheme.colorScheme.primary
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(40.dp).background(preview, CircleShape))
        Text("自由调色", style = MaterialTheme.typography.bodyLarge)
    }
    Text("色相")
    Slider(value = hue, onValueChange = { hue = it; updateHex() }, valueRange = 0f..360f)
    Text("饱和度")
    Slider(value = saturation, onValueChange = { saturation = it; updateHex() }, valueRange = 0f..1f)
    Text("明度")
    Slider(value = brightness, onValueChange = { brightness = it; updateHex() }, valueRange = 0f..1f)
    OutlinedTextField(
        value = hex,
        onValueChange = { value ->
            hex = value.uppercase().take(7)
            if (hex.matches(Regex("#[0-9A-Fa-f]{6}"))) {
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(hex), hsv)
                hue = hsv[0]; saturation = hsv[1]; brightness = hsv[2]
            }
        },
        label = { Text("HEX 颜色（#RRGGBB）") },
        singleLine = true,
        isError = !valid,
        modifier = Modifier.fillMaxWidth(),
    )
    Button(onClick = { onApply(hex.uppercase()) }, enabled = valid, modifier = Modifier.fillMaxWidth()) {
        Text("应用自定义颜色")
    }
}
