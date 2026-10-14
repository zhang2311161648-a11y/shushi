package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import coil.compose.AsyncImage
import com.naveenapps.expensemanager.core.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val AssetGreen = Color(0xFFA8EF00)
private val AssetBlack = Color(0xFF191919)
private val AssetYellow = Color(0xFFFFCF47)
@Composable private fun assetBackground() = if (JiangJiColors.Ivory.luminance() < .3f) MaterialTheme.colorScheme.surfaceContainerLowest else Color(0xFFF5F5F5)
@Composable private fun assetSurface() = if (JiangJiColors.Ivory.luminance() < .3f) MaterialTheme.colorScheme.surface else Color.White
internal fun AnxietyReliefItem.statusLabel() = if (isArchived || isSold) "已归档" else "陪伴中"
private fun statusColor(status: String) = when (status) { "陪伴中" -> AssetGreen; "已归档" -> AssetYellow; else -> Color(0xFF888888) }
private fun assetMoney(cents: Long, currency: Currency?) = "${currency?.symbol ?: "¥"}${BigDecimal.valueOf(cents, 2).setScale(2).toPlainString()}"

internal fun purchaseMoodEmoji(mood: String): String {
    if (mood.isBlank() || mood.trim() == "未填写") return ""
    val symbols = buildString {
        mood.codePoints().forEach { code ->
            if (code in 0x1F000..0x1FAFF || code in 0x2600..0x27BF || code == 0x200D || code == 0xFE0F) appendCodePoint(code)
        }
    }.trim('\u200D', '\uFE0F')
    if (symbols.isNotEmpty()) return symbols
    return when {
        mood.isBlank() || mood == "未填写" -> ""
        "开心" in mood -> "🥰"
        "心动" in mood -> "💖"
        "奖励" in mood -> "✨"
        "治愈" in mood -> "🌿"
        "平静" in mood -> "☕"
        "冲动" in mood -> "⚡"
        else -> "💭"
    }
}

@OptIn(ExperimentalTime::class)
@Composable
internal fun AnxietyAssetOverview(
    state: JiangJiAnxietyReliefState,
    onAdd: () -> Unit,
    onDetails: (AnxietyReliefItem) -> Unit,
    onRecordUse: (AnxietyReliefItem) -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (AnxietyReliefItem) -> Unit,
    onDelete: (AnxietyReliefItem) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("全部") }
    var status by rememberSaveable { mutableStateOf("全部") }
    var sort by rememberSaveable { mutableStateOf("最近添加") }
    var showSort by remember { mutableStateOf(false) }
    var onlyPending by rememberSaveable { mutableStateOf(false) }
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val categories = listOf("全部") + state.items.map { it.category }.distinct().sorted()
    val filtered = state.items.filter {
        (category == "全部" || it.category == category) && (status == "全部" || it.statusLabel() == status) &&
            (query.isBlank() || it.name.contains(query, true) || it.note.contains(query, true)) &&
            (!onlyPending || assetProgress(it, today) < 1f)
    }.let { list -> when (sort) {
        "价格从高到低" -> list.sortedByDescending { it.priceCents }
        "成本从低到高" -> list.sortedBy { assetAverage(it, today) ?: Long.MAX_VALUE }
        "购买日期" -> list.sortedByDescending { it.purchaseDate }
        else -> list.sortedByDescending { it.createdAt }
    } }
    val pageBackground = if (MaterialTheme.colorScheme.background.alpha == 0f) Color.Transparent else assetBackground()
    Box(modifier.background(pageBackground)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("焦虑抚平器", Modifier.weight(1f), fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) query = "" }) {
                        Icon(Icons.Outlined.Search, "搜索物品")
                    }
                }
                Text("好的物品会陪伴你很久很久", modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (showSearch) item(span = { GridItemSpan(maxLineSpan) }) {
                OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("搜索名称或备注") }, singleLine = true)
            }
            item(span = { GridItemSpan(maxLineSpan) }) { AssetSummary(state.items, today, state.currency) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    categories.forEach { label ->
                        TextButton(onClick = { category = label }, contentPadding = PaddingValues(vertical = 6.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, color = if (category == label) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 16.sp, fontWeight = if (category == label) FontWeight.Bold else FontWeight.Normal)
                                Spacer(Modifier.height(3.dp))
                                Box(Modifier.width(28.dp).height(4.dp).clip(CircleShape).background(if (category == label) AssetGreen else Color.Transparent))
                            }
                        }
                    }
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("全部", "陪伴中", "已归档").forEach { label ->
                            Surface(onClick = { status = label }, shape = CircleShape,
                                color = if (status == label) AssetBlack else assetSurface()) {
                                Text(label, Modifier.padding(horizontal = 12.dp, vertical = 11.dp), fontSize = 12.sp,
                                    color = if (status == label) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { showSort = true }) { Icon(Icons.Outlined.Sort, "排序：$sort") }
                        DropdownMenu(showSort, { showSort = false }) {
                            listOf("最近添加", "价格从高到低", "成本从低到高", "购买日期").forEach { label ->
                                DropdownMenuItem(text = { Text(label) }, onClick = { sort = label; showSort = false })
                            }
                        }
                    }
                    IconToggleButton(checked = onlyPending, onCheckedChange = { onlyPending = it }) {
                        Icon(Icons.Outlined.FilterAlt, "仅显示目标未达成", tint = if (onlyPending) Color(0xFF619000) else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            state.errorMessage?.let { error -> item(span = { GridItemSpan(maxLineSpan) }) { Text(error, color = MaterialTheme.colorScheme.error) } }
            if (filtered.isEmpty()) item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth().padding(vertical = 38.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Inventory2, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(if (state.items.isEmpty()) "记录一件物品，看见它陪伴你的时间" else "没有符合条件的物品")
                    TextButton(onClick = if (state.items.isEmpty()) onAdd else { { query = ""; category = "全部"; status = "全部"; onlyPending = false } }) {
                        Text(if (state.items.isEmpty()) "添加第一件物品" else "清除筛选")
                    }
                }
            }
            items(filtered, key = { it.id }) { item ->
                AssetGridCard(item, today, state.currency, { onDetails(item) }, { onRecordUse(item) }, { onEdit(item) }, { onDelete(item) })
            }
        }
        FloatingActionButton(onClick = onAdd, containerColor = AssetBlack, contentColor = Color.White,
            shape = CircleShape, modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp).size(62.dp)) {
            Icon(Icons.Outlined.Add, "添加物品", Modifier.size(30.dp))
        }
    }
}

@Composable
private fun AssetSummary(items: List<AnxietyReliefItem>, today: LocalDate, currency: Currency?) {
    val summaryColor = remember(MaterialTheme.colorScheme.primary) { JiangJiColors.DeepBurgundy }
    val total = items.fold(BigDecimal.ZERO) { sum, item -> sum + BigDecimal.valueOf(item.priceCents, 2) }
    val daily = items.filter { !it.costByUse && !it.isArchived && !it.isSold }
        .fold(BigDecimal.ZERO) { sum, item -> sum + BigDecimal.valueOf(item.priceCents, 2)
            .divide(BigDecimal.valueOf(AnxietyReliefCalculator.calculate(item, today).ownedDays), 6, RoundingMode.HALF_UP) }
    Box(Modifier.fillMaxWidth().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .clip(RoundedCornerShape(26.dp)).background(summaryColor)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("资产总览", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                Text("${items.size} 件", color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.clip(CircleShape).background(Color.White.copy(alpha = .06f)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Row(Modifier.fillMaxWidth()) {
                SummaryNumber("总购入金额", "${currency?.symbol ?: "¥"}${total.setScale(2, RoundingMode.HALF_UP)}", Modifier.weight(1f))
                SummaryNumber("陪伴中日均成本", "${currency?.symbol ?: "¥"}${daily.setScale(2, RoundingMode.HALF_UP)}", Modifier.weight(1f))
            }
            Canvas(Modifier.fillMaxWidth().height(1.dp)) {
                var x = 0f
                while (x < size.width) { drawLine(Color.White.copy(alpha = .13f), Offset(x, 0f), Offset(x + 5.dp.toPx(), 0f)); x += 10.dp.toPx() }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("陪伴中", "已归档").forEach { label ->
                    val count = items.count { it.statusLabel() == label }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("$label $count", color = Color.White.copy(alpha = .7f), fontSize = 12.sp)
                        Canvas(Modifier.fillMaxWidth().height(5.dp)) {
                            val y = size.height / 2
                            drawLine(Color.White.copy(alpha = .12f), Offset(0f,y), Offset(size.width,y), size.height, androidx.compose.ui.graphics.StrokeCap.Round)
                            if (count > 0) drawLine(statusColor(label), Offset(0f,y), Offset(size.width * count / items.size,y), size.height, androidx.compose.ui.graphics.StrokeCap.Round)
                        }
                    }
                }
            }
        }
        Canvas(Modifier.matchParentSize()) {
            drawCircle(Color.Transparent, 6.dp.toPx(), Offset(0f, size.height * .66f), blendMode = BlendMode.Clear)
            drawCircle(Color.Transparent, 6.dp.toPx(), Offset(size.width, size.height * .66f), blendMode = BlendMode.Clear)
        }
    }
}

@Composable
private fun SummaryNumber(label: String, value: String, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color.White.copy(alpha = .8f), fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

internal fun assetAverage(item: AnxietyReliefItem, today: LocalDate): Long? =
    if (item.costByUse) item.useCount.takeIf { it > 0 }?.let { item.priceCents / it }
    else AnxietyReliefCalculator.calculate(item, today).avgDailyCostCents

internal fun assetProgress(item: AnxietyReliefItem, today: LocalDate): Float {
    val metrics = AnxietyReliefCalculator.calculate(item, today)
    val count = if (item.costByUse) item.useCount else metrics.ownedDays
    return if (metrics.targetTotalDays == 0L) 1f else (count.toDouble() / metrics.targetTotalDays).coerceIn(0.0, 1.0).toFloat()
}

@Composable
private fun AssetGridCard(item: AnxietyReliefItem, today: LocalDate, currency: Currency?, onClick: () -> Unit, onUse: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val metrics = AnxietyReliefCalculator.calculate(item, today)
    val progress = assetProgress(item, today)
    val moodEmoji = purchaseMoodEmoji(item.mood)
    var showActions by remember { mutableStateOf(false) }
    Card(onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = assetSurface())) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                if (item.imageUri != null) AsyncImage(item.imageUri, "${item.name}图片", Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Fit)
                else Box(Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)).background(assetBackground()), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Inventory2, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.weight(1f))
                Row(Modifier.clip(CircleShape).background(assetBackground()).padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(5.dp).clip(CircleShape).background(statusColor(item.statusLabel())))
                    Text(item.statusLabel(), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(item.name, Modifier.weight(1f), fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box {
                    IconButton(onClick = { showActions = true }) { Icon(Icons.Outlined.MoreVert, "${item.name}更多操作") }
                    DropdownMenu(expanded = showActions, onDismissRequest = { showActions = false }) {
                        DropdownMenuItem(text = { Text("编辑") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                            onClick = { showActions = false; onEdit() })
                        DropdownMenuItem(text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showActions = false; onDelete() })
                    }
                }
            }
            Text("${assetMoney(item.priceCents, currency)} · ${if (item.costByUse) "${item.useCount} 次" else "${metrics.ownedDays} 天"}",
                fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (moodEmoji.isNotEmpty()) Row(Modifier.padding(top = 8.dp).semantics { contentDescription = "购入心情：${item.mood}" },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("购入心情", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(moodEmoji, fontSize = 17.sp)
            }
            Spacer(Modifier.height(if (moodEmoji.isNotEmpty()) 10.dp else 23.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${assetAverage(item, today)?.let { assetMoney(it, currency) } ?: "—"}/${if (item.costByUse) "次" else "天"}",
                    Modifier.weight(1f), fontSize = if (item.costByUse) 16.sp else 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.costByUse && !item.isArchived && !item.isSold) IconButton(onClick = onUse, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.AddCircle, "${item.name}使用一次", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(Modifier.height(14.dp))
            Canvas(Modifier.fillMaxWidth().height(15.dp)) {
                val y = size.height * .65f
                drawLine(Color(0xFFECECEC), Offset(0f,y), Offset(size.width,y), 5.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                if (progress > 0f) drawLine(AssetGreen, Offset(0f,y), Offset(size.width * progress,y), 5.dp.toPx(), androidx.compose.ui.graphics.StrokeCap.Round)
                val x = (size.width * progress).coerceIn(4.dp.toPx(), size.width - 4.dp.toPx())
                drawPath(Path().apply { moveTo(x - 4.dp.toPx(),0f); lineTo(x + 4.dp.toPx(),0f); lineTo(x,y - 2.dp.toPx()); close() }, AssetBlack)
            }
            val remaining = if (item.costByUse) (metrics.targetTotalDays - item.useCount).coerceAtLeast(0) else metrics.remainingDays
            Text(if (progress >= 1f) "目标已达成" else "还剩 $remaining ${if (item.costByUse) "次" else "天"}",
                Modifier.fillMaxWidth().padding(top = 4.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
