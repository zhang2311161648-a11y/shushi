package com.naveenapps.expensemanager.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import com.naveenapps.expensemanager.core.model.AnxietyReliefCalculator
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.AnxietyReliefMetrics
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import java.math.BigDecimal
import java.util.Date
import kotlin.time.ExperimentalTime

enum class JiangJiAssetsSection {
    AnxietyRelief,
    Savings,
    Calendar,
}

enum class JiangJiAnxietyReliefPage {
    Home,
    Editor,
    Details,
}

@Composable
fun JiangJiAssetSectionTabs(
    selected: JiangJiAssetsSection,
    onSelected: (JiangJiAssetsSection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        JiangJiAssetSectionTab(
            title = "焦虑抚平器",
            selected = selected == JiangJiAssetsSection.AnxietyRelief,
            onClick = { onSelected(JiangJiAssetsSection.AnxietyRelief) },
            modifier = Modifier.weight(1f),
        )
        JiangJiAssetSectionTab(
            title = "存钱计划",
            selected = selected == JiangJiAssetsSection.Savings,
            onClick = { onSelected(JiangJiAssetsSection.Savings) },
            modifier = Modifier.weight(1f),
        )
        JiangJiAssetSectionTab(
            title = "收支日历",
            selected = selected == JiangJiAssetsSection.Calendar,
            onClick = { onSelected(JiangJiAssetsSection.Calendar) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun JiangJiAssetSectionTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) JiangJiColors.Burgundy else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun JiangJiAnxietyReliefScreen(
    viewModel: JiangJiAnxietyReliefViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var page by remember { mutableStateOf(JiangJiAnxietyReliefPage.Home) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }
    val selectedItem = state.items.firstOrNull { it.id == selectedItemId }
    var pendingDelete by remember { mutableStateOf<AnxietyReliefItem?>(null) }

    BackHandler(enabled = page != JiangJiAnxietyReliefPage.Home) {
        page = JiangJiAnxietyReliefPage.Home
    }

    LaunchedEffect(state.editor, page) {
        if (page == JiangJiAnxietyReliefPage.Editor && state.editor == null) {
            page = JiangJiAnxietyReliefPage.Home
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (page) {
            JiangJiAnxietyReliefPage.Home -> JiangJiAnxietyReliefHome(
                state = state,
                onRecordUse = viewModel::recordUse,
                onDelete = { pendingDelete = it },
                onOpenNew = {
                    viewModel.openNew()
                    page = JiangJiAnxietyReliefPage.Editor
                },
                onOpenEdit = { item ->
                    viewModel.openEdit(item)
                    selectedItemId = item.id
                    page = JiangJiAnxietyReliefPage.Editor
                },
                onOpenDetails = { item ->
                    selectedItemId = item.id
                    page = JiangJiAnxietyReliefPage.Details
                },
                modifier = Modifier.weight(1f),
            )

            JiangJiAnxietyReliefPage.Editor -> {
                val editor = state.editor
                if (editor != null) {
                    JiangJiAnxietyReliefEditor(
                        editor = editor,
                        onBack = viewModel::closeEditor,
                        onPickImage = { uri -> viewModel.updateImageUri(uri.toString()) },
                        onNameChanged = viewModel::updateName,
                        onPriceChanged = viewModel::updatePrice,
                        onDateChanged = viewModel::updatePurchaseDate,
                        onMoodChanged = viewModel::updateMood,
                        onNoteChanged = viewModel::updateNote,
                        onCategoryChanged = viewModel::updateCategory,
                        onStatusChanged = viewModel::updateStatus,
                        onCostByUseChanged = viewModel::updateCostByUse,
                        onUseCountChanged = viewModel::updateUseCount,
                        onTargetChanged = viewModel::updateTargetDailyCost,
                        onSave = viewModel::saveEditor,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            JiangJiAnxietyReliefPage.Details -> {
                if (selectedItem != null) {
                    JiangJiAnxietyReliefDetails(
                        item = selectedItem,
                        currency = state.currency,
                        onImageChanged = { uri -> viewModel.changeItemImage(selectedItem.id, uri.toString()) },
                        isChangingImage = state.isChangingImage,
                        errorMessage = state.errorMessage,
                        onBack = { page = JiangJiAnxietyReliefPage.Home },
                        onEdit = {
                            viewModel.openEdit(selectedItem)
                            page = JiangJiAnxietyReliefPage.Editor
                        },
                        onArchive = {
                            viewModel.archive(selectedItem) { page = JiangJiAnxietyReliefPage.Home }
                        },
                        onDelete = { pendingDelete = selectedItem },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    pendingDelete?.let { item ->
        AnxietyDeleteDialog(
            item = item,
            isDeleting = state.isDeleting,
            errorMessage = state.errorMessage,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                viewModel.delete(item) {
                    pendingDelete = null
                    if (selectedItemId == item.id) page = JiangJiAnxietyReliefPage.Home
                }
            },
        )
    }
}

@Composable
internal fun AnxietyDeleteDialog(
    item: AnxietyReliefItem,
    isDeleting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text("删除物品？") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("确定删除「${item.name}」？该物品及其使用记录将被删除，无法撤销。收支记录不受影响。")
                errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                Text(if (isDeleting) "删除中…" else "删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isDeleting) { Text("取消") } },
    )
}

@Composable
private fun JiangJiAnxietyReliefHome(
    state: JiangJiAnxietyReliefState,
    onOpenNew: () -> Unit,
    onOpenEdit: (AnxietyReliefItem) -> Unit,
    onOpenDetails: (AnxietyReliefItem) -> Unit,
    onRecordUse: (AnxietyReliefItem) -> Unit,
    onDelete: (AnxietyReliefItem) -> Unit,
    modifier: Modifier = Modifier,
) = AnxietyAssetOverview(state, onOpenNew, onOpenDetails, onRecordUse, modifier, onEdit = onOpenEdit, onDelete = onDelete)

@Composable
private fun JiangJiAnxietyReliefEditor(
    editor: JiangJiAnxietyReliefEditorState,
    onBack: () -> Unit,
    onPickImage: (android.net.Uri) -> Unit,
    onNameChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onDateChanged: (String) -> Unit,
    onMoodChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onCategoryChanged: (String) -> Unit,
    onStatusChanged: (String) -> Unit,
    onCostByUseChanged: (Boolean) -> Unit,
    onUseCountChanged: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var showDatePicker by remember { mutableStateOf(false) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
            onPickImage(it)
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            JiangJiSubPageHeader(
                title = if (editor.itemId == null) "添加物品" else "编辑物品",
                onBack = onBack,
                action = {
                    TextButton(onClick = onSave, enabled = !editor.isSaving) { Text(if (editor.isSaving) "保存中…" else "保存") }
                },
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AnxietyReliefImage(uri = editor.imageUri, modifier = Modifier.size(128.dp))
                    TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (editor.imageUri == null) "从本地相册选择" else "更换图片")
                    }
                }
            }
        }
        item {
            OutlinedTextField(value = editor.name, onValueChange = onNameChanged, label = { Text("物品名称") }, placeholder = { Text("例如：一台相机") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = editor.price, onValueChange = onPriceChanged, label = { Text("价格") }, placeholder = { Text("请输入价格") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("使用日期", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(editor.purchaseDate, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "点击切换年份、月份和具体天数",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "得到该物品时是什么心情？",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                val moodOptions = listOf("开心 🥰", "心动 💖", "犒劳 ✨", "治愈 🌿", "平静 ☕", "冲动 ⚡", "期待 🎈", "解压 🌸")
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(moodOptions) { mood ->
                        val isSelected = editor.mood == mood
                        Surface(
                            onClick = { onMoodChanged(mood) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) JiangJiColors.Burgundy else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) JiangJiColors.Burgundy else MaterialTheme.colorScheme.outlineVariant,
                            ),
                        ) {
                            Text(
                                text = mood,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = editor.mood,
                    onValueChange = onMoodChanged,
                    label = { Text("心情表达") },
                    placeholder = { Text("也可输入自定义心情，如：超值 🌟") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            OutlinedTextField(value = editor.category, onValueChange = onCategoryChanged, label = { Text("分类") },
                placeholder = { Text("例如：数码设备、每日通勤") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("陪伴中", "已归档").forEach { status ->
                    androidx.compose.material3.FilterChip(selected = editor.status == status, onClick = { onStatusChanged(status) }, label = { Text(status) })
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("按使用次数计算成本", Modifier.weight(1f))
                androidx.compose.material3.Switch(checked = editor.costByUse, onCheckedChange = onCostByUseChanged)
            }
        }
        if (editor.costByUse) item {
            OutlinedTextField(value = editor.useCount, onValueChange = onUseCountChanged, label = { Text("已使用次数") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = editor.targetDailyCost, onValueChange = onTargetChanged,
                label = { Text(if (editor.costByUse) "目标每次成本" else "目标日均成本") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = editor.note, onValueChange = onNoteChanged, label = { Text("备注（可选）") }, minLines = 3, maxLines = 5, modifier = Modifier.fillMaxWidth())
        }
        editor.validationError?.let { error ->
            item { Text(error, color = JiangJiColors.DeepBurgundy, style = MaterialTheme.typography.bodySmall) }
        }
        item {
            Button(onClick = onSave, enabled = !editor.isSaving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = JiangJiColors.Burgundy)) {
                Text(if (editor.isSaving) "保存中…" else "保存")
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            selectedDate = isoToDate(editor.purchaseDate),
            onDateSelected = { onDateChanged(dateToIso(it)); showDatePicker = false },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun JiangJiAnxietyReliefDetails(
    item: AnxietyReliefItem,
    currency: Currency?,
    onImageChanged: (android.net.Uri) -> Unit,
    isChangingImage: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val metrics = AnxietyReliefCalculator.calculate(item, today)
    var showArchiveDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var imagePermissionError by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val permission = runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (permission.isSuccess) {
                imagePermissionError = null
                onImageChanged(uri)
            } else imagePermissionError = "无法保存图片访问权限，请重新选择图片"
        }
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            JiangJiSubPageHeader(
                title = "物品详情",
                onBack = onBack,
                action = {
                    Row {
                        IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, contentDescription = "编辑", tint = JiangJiColors.Burgundy) }
                        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error) }
                    }
                },
            )
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnxietyReliefImage(uri = item.imageUri, modifier = Modifier.size(190.dp))
                    TextButton(onClick = { imagePicker.launch(arrayOf("image/*")) }, enabled = !isChangingImage) {
                        Icon(Icons.Outlined.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(if (isChangingImage) "正在保存图片…" else "更换图片")
                    }
                    Text(item.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    if (item.note.isNotBlank()) Text(item.note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        (imagePermissionError ?: errorMessage)?.let { error ->
            item { Text(error, color = MaterialTheme.colorScheme.error) }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    JiangJiDetailLine("价格", formatCents(item.priceCents, currency))
                    JiangJiDetailLine("使用日期", item.purchaseDate)
                    JiangJiDetailLine("已拥有天数", "${metrics.ownedDays} 天")
                    JiangJiDetailLine("当前日均金额", formatCents(metrics.avgDailyCostCents, currency))
                    JiangJiDetailLine("购入心情", item.mood)
                }
            }
        }
        item {
            Text("每一次使用，都是这件物品在认真陪伴你。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            TextButton(onClick = { showArchiveDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Archive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("归档物品", color = JiangJiColors.DeepBurgundy)
            }
        }
    }

    if (showArchiveDialog) {
        AlertDialog(
            onDismissRequest = { showArchiveDialog = false },
            title = { Text("归档物品？") },
            text = { Text("归档后仍可在已归档列表中查看，所有物品记录和收支数据都会保留。") },
            confirmButton = { TextButton(onClick = { showArchiveDialog = false; onArchive() }) { Text("归档") } },
            dismissButton = { TextButton(onClick = { showArchiveDialog = false }) { Text("取消") } },
        )
    }
}

@Composable
private fun JiangJiSubPageHeader(title: String, onBack: () -> Unit, action: @Composable () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Outlined.ArrowBack, contentDescription = "返回") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        action()
    }
}

@Composable
private fun JiangJiDetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AnxietyReliefImage(uri: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(JiangJiColors.SecondarySurface),
        contentAlignment = Alignment.Center,
    ) {
        if (uri.isNullOrBlank()) {
            Icon(Icons.Outlined.Image, contentDescription = null, tint = JiangJiColors.Burgundy, modifier = Modifier.size(32.dp))
        } else {
            AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

internal enum class PixelCatState { IDLE, BLINK, HAPPY, SLEEP }

@Composable
internal fun PixelCat(state: PixelCatState, modifier: Modifier = Modifier) {
    val ivory = Color(0xFFF7F0E5)
    val gray = Color(0xFFBEB6AD)
    Canvas(modifier = modifier) {
        val cell = size.minDimension / 10f
        fun pixel(x: Int, y: Int, color: Color = ivory) {
            drawRect(color, Offset(x * cell, y * cell), Size(cell, cell))
        }
        for (x in 2..7) for (y in 2..7) pixel(x, y, gray)
        pixel(2, 1); pixel(3, 2); pixel(6, 2); pixel(7, 1)
        pixel(3, 3); pixel(6, 3)
        when (state) {
            PixelCatState.BLINK, PixelCatState.SLEEP -> {
                pixel(3, 4, JiangJiColors.DeepBurgundy); pixel(6, 4, JiangJiColors.DeepBurgundy)
            }
            else -> {
                pixel(3, 4, JiangJiColors.DeepBurgundy); pixel(6, 4, JiangJiColors.DeepBurgundy)
            }
        }
        pixel(4, 5, JiangJiColors.DeepBurgundy); pixel(5, 5, JiangJiColors.DeepBurgundy)
        if (state == PixelCatState.HAPPY) {
            pixel(3, 6); pixel(4, 7); pixel(5, 7); pixel(6, 6)
        } else {
            pixel(4, 6, JiangJiColors.DeepBurgundy); pixel(5, 6, JiangJiColors.DeepBurgundy)
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun isoToDate(value: String): Date = runCatching {
    LocalDate.parse(value).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds().let(::Date)
}.getOrDefault(Date())

@OptIn(ExperimentalTime::class)
private fun dateToIso(value: Date): String = kotlin.time.Instant.fromEpochMilliseconds(value.time)
    .toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

private fun currencySymbol(currency: Currency?): String = currency?.symbol ?: "¥"

private fun formatCents(cents: Long, currency: Currency?): String {
    val amount = BigDecimal.valueOf(cents, 2).stripTrailingZeros().toPlainString()
    val symbol = currencySymbol(currency)
    return if (currency?.position == CurrencyPosition.SUFFIX) "$amount$symbol" else "$symbol$amount"
}
