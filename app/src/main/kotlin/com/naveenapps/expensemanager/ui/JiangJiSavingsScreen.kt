package com.naveenapps.expensemanager.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naveenapps.expensemanager.core.designsystem.ui.components.SafeModalBottomSheet
import com.naveenapps.expensemanager.core.model.Currency
import com.naveenapps.expensemanager.core.model.CurrencyPosition
import com.naveenapps.expensemanager.core.model.parseSavingsAmountCents
import com.naveenapps.expensemanager.core.model.nextSuggestedAmountCents
import com.naveenapps.expensemanager.core.model.effectiveBaseAmountCents
import com.naveenapps.expensemanager.core.model.SavingsPlan
import com.naveenapps.expensemanager.core.model.SavingsPlanType
import com.naveenapps.expensemanager.core.model.Transaction
import com.naveenapps.expensemanager.core.model.TransactionType
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Calendar
import java.util.Locale

@Composable
fun JiangJiAssetsScreen(
    savingsViewModel: JiangJiSavingsViewModel = koinViewModel(),
    anxietyReliefViewModel: JiangJiAnxietyReliefViewModel = koinViewModel(),
    onNavigateToCreateTransaction: () -> Unit = {},
) {
    val savingsState by savingsViewModel.state.collectAsState()
    var section by remember { mutableStateOf(JiangJiAssetsSection.AnxietyRelief) }

    Column(modifier = Modifier.fillMaxSize()) {
        JiangJiAssetSectionTabs(
            selected = section,
            onSelected = { section = it },
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        )
        when (section) {
            JiangJiAssetsSection.AnxietyRelief -> JiangJiAnxietyReliefScreen(
                viewModel = anxietyReliefViewModel,
                modifier = Modifier.weight(1f),
            )

            JiangJiAssetsSection.Savings -> JiangJiSavingsPlanHome(
                state = savingsState,
                viewModel = savingsViewModel,
                modifier = Modifier.weight(1f),
            )

            JiangJiAssetsSection.Calendar -> JiangJiCalendarHome(
                state = savingsState,
                viewModel = savingsViewModel,
                onNavigateToCreateTransaction = onNavigateToCreateTransaction,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/* =========================================================================================
 * 1. 存钱计划 (Savings Plans: 定额、灵活、52周、365天)
 * ========================================================================================= */

@Composable
private fun JiangJiSavingsPlanHome(
    state: JiangJiSavingsState,
    viewModel: JiangJiSavingsViewModel,
    modifier: Modifier = Modifier,
) {
    var creatingPlanType by remember { mutableStateOf<SavingsPlanType?>(null) }
    var depositingPlan by remember { mutableStateOf<SavingsPlan?>(null) }
    var showLegacyHistory by remember { mutableStateOf(false) }
    var planToDelete by remember { mutableStateOf<SavingsPlan?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 14.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 顶部 4 种存钱法入口
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "存钱方法",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SavingsMethodCard(
                        title = "定额存钱法",
                        badge = "定期固定",
                        icon = Icons.Outlined.AccountBalanceWallet,
                        iconTint = Color(0xFF5C6BC0),
                        onClick = { creatingPlanType = SavingsPlanType.FIXED },
                        modifier = Modifier.weight(1f),
                    )
                    SavingsMethodCard(
                        title = "灵活存钱法",
                        badge = "随心储蓄",
                        icon = Icons.Outlined.WaterDrop,
                        iconTint = Color(0xFF29B6F6),
                        onClick = { creatingPlanType = SavingsPlanType.FLEXIBLE },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    SavingsMethodCard(
                        title = "52周存钱法",
                        badge = "每周递增",
                        icon = Icons.Outlined.CalendarMonth,
                        iconTint = Color(0xFF7E57C2),
                        onClick = { creatingPlanType = SavingsPlanType.WEEK_52 },
                        modifier = Modifier.weight(1f),
                    )
                    SavingsMethodCard(
                        title = "365天存钱法",
                        badge = "每日打卡",
                        icon = Icons.Outlined.WbSunny,
                        iconTint = Color(0xFF66BB6A),
                        onClick = { creatingPlanType = SavingsPlanType.DAY_365 },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        // 我的计划列表
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "我的计划",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "共 ${state.plans.size} 个计划",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.legacySavings.isNotEmpty()) {
            item {
                OutlinedButton(onClick = { showLegacyHistory = !showLegacyHistory }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (showLegacyHistory) "收起旧版储蓄历史" else "查看旧版储蓄历史（${state.legacySavings.size} 条）")
                }
            }
            if (showLegacyHistory) {
                items(state.legacySavings, key = { "legacy-${it.id}" }) { saving ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${saving.year}年${saving.month}月 · ${formatMoney(saving.amountCents, state.currency)}")
                            saving.note?.takeIf { it.isNotBlank() }?.let { Text(it) }
                            Text("旧版储蓄记录", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        if (state.plans.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Savings,
                            contentDescription = null,
                            tint = JiangJiColors.Burgundy,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "暂无进行中的存钱计划",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "点击上方 4 种存钱法，定制适合你的存款目标吧！",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(state.plans, key = { it.id }) { plan ->
                SavingsPlanItemCard(
                    plan = plan,
                    currency = state.currency,
                    onDeposit = { depositingPlan = plan },
                    onDelete = { planToDelete = plan },
                )
            }
        }
    }

    // 创建计划弹窗
    creatingPlanType?.let { type ->
        CreatePlanDialog(
            type = type,
            currency = state.currency,
            onDismiss = { creatingPlanType = null },
            onCreate = { name, targetCents, startDate, endDate, frequency, note ->
                viewModel.createPlan(
                    name = name,
                    type = type,
                    targetAmountCents = targetCents,
                    startDate = startDate,
                    endDate = endDate,
                    frequency = frequency,
                    note = note,
                )
                creatingPlanType = null
            },
        )
    }

    // 存一笔弹窗
    depositingPlan?.let { plan ->
        DepositDialog(
            plan = plan,
            currency = state.currency,
            onDismiss = { depositingPlan = null },
            onDeposit = { amountCents, date ->
                viewModel.deposit(plan.id, amountCents, date)
                depositingPlan = null
            },
        )
    }

    // 删除计划确认弹窗
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("删除存钱计划？") },
            text = { Text("确定要删除「${plan.name}」吗？已存入数据将不会保留。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlan(plan.id)
                        planToDelete = null
                    },
                ) {
                    Text("删除", color = JiangJiColors.DeepBurgundy)
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun SavingsMethodCard(
    title: String,
    badge: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SavingsPlanItemCard(
    plan: SavingsPlan,
    currency: Currency?,
    onDeposit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCheckIns by remember(plan.id) { mutableStateOf(false) }
    val remainingCents = (plan.targetAmountCents - plan.savedAmountCents).coerceAtLeast(0L)
    val progress = if (plan.targetAmountCents > 0L) {
        (plan.savedAmountCents.toFloat() / plan.targetAmountCents.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val percentString = String.format(Locale.getDefault(), "%.1f%%", progress * 100f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 头部：图标、名称、标签、删除
            if (plan.checkIns.isNotEmpty()) {
                TextButton(onClick = { showCheckIns = !showCheckIns }) {
                    Text(if (showCheckIns) "收起打卡记录" else "查看 ${plan.checkIns.size} 次打卡记录")
                }
                if (showCheckIns) plan.checkIns.reversed().forEach { checkIn ->
                    Text("${checkIn.date} · ${formatMoney(checkIn.amountCents, currency)}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JiangJiColors.Burgundy.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Savings,
                        contentDescription = null,
                        tint = JiangJiColors.Burgundy,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = JiangJiColors.Burgundy.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = "${plan.type.badge} · ${plan.frequency}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = JiangJiColors.Burgundy,
                            )
                        }
                    }
                    Text(
                        text = "${plan.startDate} - ${plan.endDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "删除计划",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Text(
                text = "目标金额: ${formatMoney(plan.targetAmountCents, currency)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 两栏数据：已存金额 vs 剩余未存
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF43A047)))
                        Text("已存金额", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = formatMoney(plan.savedAmountCents, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(JiangJiColors.DeepBurgundy))
                        Text("剩余未存", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(
                        text = formatMoney(remainingCents, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            // 进度条与百分比
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("进度", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(percentString, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = JiangJiColors.Burgundy)
                }
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = JiangJiColors.Burgundy,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            // 存一笔按钮
            Button(
                onClick = onDeposit,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = JiangJiColors.Burgundy),
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("存一笔 / 打卡存钱")
            }
        }
    }
}

@Composable
private fun CreatePlanDialog(
    type: SavingsPlanType,
    currency: Currency?,
    onDismiss: () -> Unit,
    onCreate: (name: String, targetCents: Long, startDate: String, endDate: String, frequency: String, note: String) -> Unit,
) {
    var name by remember {
        mutableStateOf(
            when (type) {
                SavingsPlanType.FIXED -> "定额储蓄计划"
                SavingsPlanType.FLEXIBLE -> "灵活存钱计划"
                SavingsPlanType.WEEK_52 -> "52周递增计划"
                SavingsPlanType.DAY_365 -> "365天打卡计划"
            }
        )
    }
    var targetText by remember {
        mutableStateOf(
            when (type) {
                SavingsPlanType.FIXED -> "10000"
                SavingsPlanType.FLEXIBLE -> "5000"
                SavingsPlanType.WEEK_52 -> "13780" // 10+20+...+520 = 13,780
                SavingsPlanType.DAY_365 -> "66795" // 1+2+...+365 = 66,795
            }
        )
    }
    var frequency by remember {
        mutableStateOf(
            when (type) {
                SavingsPlanType.FIXED -> "每周"
                SavingsPlanType.FLEXIBLE -> "随心自由"
                SavingsPlanType.WEEK_52 -> "每周递增"
                SavingsPlanType.DAY_365 -> "每天打卡"
            }
        )
    }
    var startDate by remember { mutableStateOf("2026年1月1日") }
    var endDate by remember { mutableStateOf("2026年12月31日") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(type.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = type.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("计划名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("目标金额") },
                    prefix = { Text(currency?.symbol ?: "¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("周期频次（如每周、每月、每天）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("开始时间") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("结束时间") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                errorMsg?.let {
                    Text(it, color = JiangJiColors.DeepBurgundy, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = parseSavingsAmountCents(targetText)
                    if (amount == null) {
                        errorMsg = "请输入有效的目标金额"
                        return@Button
                    }
                    val targetCents = amount
                    onCreate(name, targetCents, startDate, endDate, frequency, "")
                },
                colors = ButtonDefaults.buttonColors(containerColor = JiangJiColors.Burgundy),
            ) {
                Text("创建计划")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun DepositDialog(
    plan: SavingsPlan,
    currency: Currency?,
    onDismiss: () -> Unit,
    onDeposit: (amountCents: Long, date: String) -> Unit,
) {
    var amountText by remember { mutableStateOf(plan.nextSuggestedAmountCents()?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString() }.orEmpty()) }
    var depositDate by remember { mutableStateOf(java.time.LocalDate.now().toString()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("为「${plan.name}」存入") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "当前已存 ${formatMoney(plan.savedAmountCents, currency)} / 目标 ${formatMoney(plan.targetAmountCents, currency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                plan.nextSuggestedAmountCents()?.let { suggested ->
                    val unit = if (plan.type == SavingsPlanType.WEEK_52) "周" else "天"
                    Text("第 ${plan.checkIns.size + 1} $unit · 建议存入 ${formatMoney(suggested, currency)}")
                }
                OutlinedTextField(value = depositDate, onValueChange = { depositDate = it },
                    label = { Text("打卡日期（yyyy-MM-dd）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; errorMsg = null },
                    label = { Text("本次存入金额") },
                    prefix = { Text(currency?.symbol ?: "¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMsg?.let {
                    Text(it, color = JiangJiColors.DeepBurgundy, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = parseSavingsAmountCents(amountText)
                    if (amount == null) {
                        errorMsg = "请输入大于 0 的存入金额"
                        return@Button
                    }
                    if (runCatching { java.time.LocalDate.parse(depositDate) }.isFailure) {
                        errorMsg = "请输入有效日期（yyyy-MM-dd）"
                        return@Button
                    }
                    onDeposit(amount, depositDate)
                },
                colors = ButtonDefaults.buttonColors(containerColor = JiangJiColors.Burgundy),
            ) {
                Text("确认存入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

/* =========================================================================================
 * 2. 收支日历 (Income & Expense Calendar)
 * ========================================================================================= */

@Composable
private fun JiangJiCalendarHome(
    state: JiangJiSavingsState,
    viewModel: JiangJiSavingsViewModel,
    onNavigateToCreateTransaction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, state.calendarYear)
    cal.set(Calendar.MONTH, state.calendarMonth - 1)
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon, ..., 6=Sat
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // 计算当月总收支
    var monthExpenseCents = 0L
    var monthIncomeCents = 0L
    val dailyExpenseMap = mutableMapOf<Int, Long>()
    val dailyIncomeMap = mutableMapOf<Int, Long>()
    val dailyTxMap = mutableMapOf<Int, MutableList<Transaction>>()

    val itemCal = Calendar.getInstance()
    state.allTransactions.forEach { tx ->
        itemCal.time = tx.createdOn
        val txYear = itemCal.get(Calendar.YEAR)
        val txMonth = itemCal.get(Calendar.MONTH) + 1
        val txDay = itemCal.get(Calendar.DAY_OF_MONTH)

        if (txYear == state.calendarYear && txMonth == state.calendarMonth) {
            val amountCents = tx.effectiveBaseAmountCents()
            if (tx.type == TransactionType.EXPENSE) {
                monthExpenseCents += amountCents
                dailyExpenseMap[txDay] = (dailyExpenseMap[txDay] ?: 0L) + amountCents
            } else if (tx.type == TransactionType.INCOME) {
                monthIncomeCents += amountCents
                dailyIncomeMap[txDay] = (dailyIncomeMap[txDay] ?: 0L) + amountCents
            }
            dailyTxMap.getOrPut(txDay) { mutableListOf() }.add(tx)
        }
    }

    val selectedDayTxs = dailyTxMap[state.selectedDay].orEmpty()
    val selectedDayExpense = dailyExpenseMap[state.selectedDay] ?: 0L
    val selectedDayIncome = dailyIncomeMap[state.selectedDay] ?: 0L

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = 28.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 月份切换栏 + 记一笔
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = viewModel::previousMonth) {
                        Icon(Icons.Outlined.ChevronLeft, contentDescription = "上个月")
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "%04d年%02d月", state.calendarYear, state.calendarMonth),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = viewModel::nextMonth) {
                        Icon(Icons.Outlined.ChevronRight, contentDescription = "下个月")
                    }
                }
                TextButton(onClick = onNavigateToCreateTransaction) {
                    Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("记一笔", color = JiangJiColors.Burgundy, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // 月度汇总卡片
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("本月支出", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "-${formatMoney(monthExpenseCents, state.currency)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935),
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("本月收入", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "+${formatMoney(monthIncomeCents, state.currency)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF43A047),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("本月结余", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = formatMoney(monthIncomeCents - monthExpenseCents, state.currency),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        // 日历网格
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // 星期标头
                    val weekdays = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
                    Row(modifier = Modifier.fillMaxWidth()) {
                        weekdays.forEach { dayName ->
                            Text(
                                text = dayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 7列日历格子
                    val totalCells = firstDayOfWeek + daysInMonth
                    val rows = (totalCells + 6) / 7

                    for (row in 0 until rows) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNumber = cellIndex - firstDayOfWeek + 1

                                if (cellIndex in firstDayOfWeek until totalCells) {
                                    val isSelected = dayNumber == state.selectedDay
                                    val expense = dailyExpenseMap[dayNumber] ?: 0L
                                    val income = dailyIncomeMap[dayNumber] ?: 0L

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(58.dp)
                                            .padding(2.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isSelected) JiangJiColors.Burgundy.copy(alpha = 0.15f)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.dp,
                                                color = if (isSelected) JiangJiColors.Burgundy else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp),
                                            )
                                            .clickable { viewModel.selectDay(dayNumber) },
                                        contentAlignment = Alignment.TopCenter,
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(top = 3.dp),
                                        ) {
                                            Text(
                                                text = "$dayNumber",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) JiangJiColors.Burgundy else MaterialTheme.colorScheme.onSurface,
                                            )
                                            if (expense > 0L) {
                                                Text(
                                                    text = "-${formatCompact(expense)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = Color(0xFFE53935),
                                                    maxLines = 1,
                                                )
                                            }
                                            if (income > 0L) {
                                                Text(
                                                    text = "+${formatCompact(income)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = Color(0xFF43A047),
                                                    maxLines = 1,
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 选中日期的收支明细列表
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "${state.calendarMonth}月${state.selectedDay}日 收支明细",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (selectedDayExpense > 0L) {
                        Text(
                            text = "出 -${formatMoney(selectedDayExpense, state.currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE53935),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    if (selectedDayIncome > 0L) {
                        Text(
                            text = "入 +${formatMoney(selectedDayIncome, state.currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF43A047),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        if (selectedDayTxs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "当日暂无明细记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(selectedDayTxs, key = { it.id }) { tx ->
                CalendarTransactionItem(tx = tx, currency = state.currency)
            }
        }
    }
}

@Composable
private fun CalendarTransactionItem(
    tx: Transaction,
    currency: Currency?,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when (tx.type) {
                            TransactionType.EXPENSE -> Color(0xFFE53935).copy(alpha = 0.12f)
                            TransactionType.INCOME -> Color(0xFF43A047).copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tx.category.name.take(1).ifBlank { "记" },
                    fontWeight = FontWeight.Bold,
                    color = when (tx.type) {
                        TransactionType.EXPENSE -> Color(0xFFE53935)
                        TransactionType.INCOME -> Color(0xFF43A047)
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.category.name.ifBlank { "默认类别" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = listOfNotNull(tx.fromAccount.name.ifBlank { null }, tx.notes.ifBlank { null })
                        .joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            val amountStr = java.math.BigDecimal.valueOf(tx.effectiveBaseAmountCents(), 2).toPlainString()
            val symbol = currency?.symbol ?: "¥"
            val displayAmount = when (tx.type) {
                TransactionType.EXPENSE -> "-$symbol$amountStr"
                TransactionType.INCOME -> "+$symbol$amountStr"
                else -> "$symbol$amountStr"
            }
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (tx.type) {
                    TransactionType.EXPENSE -> Color(0xFFE53935)
                    TransactionType.INCOME -> Color(0xFF43A047)
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

/* =========================================================================================
 * 辅助格式化函数
 * ========================================================================================= */

private fun formatMoney(cents: Long, currency: Currency?): String {
    val df = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))
    val amount = df.format(BigDecimal.valueOf(cents, 2))
    val symbol = currency?.symbol ?: "¥"
    return if (currency?.position == CurrencyPosition.SUFFIX) "$amount$symbol" else "$symbol$amount"
}

private fun formatCompact(cents: Long): String {
    val yuan = cents / 100.0
    return if (yuan >= 1000.0) {
        String.format(Locale.getDefault(), "%.1fk", yuan / 1000.0)
    } else if (yuan == yuan.toLong().toDouble()) {
        "${yuan.toLong()}"
    } else {
        String.format(Locale.getDefault(), "%.1f", yuan)
    }
}
