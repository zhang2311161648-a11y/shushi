package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import java.time.YearMonth

@Composable
internal fun StatsMonthSelector(
    selectedMonth: YearMonth,
    onMonthChange: (YearMonth) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onMonthChange(selectedMonth.minusMonths(1)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.stats_previous_month),
            )
        }
        TextButton(
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
        ) {
            AnimatedContent(
                targetState = selectedMonth,
                label = "selected_stats_month",
            ) { month ->
                Text(
                    text = stringResource(
                        R.string.stats_selected_month,
                        month.year,
                        month.monthValue,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        IconButton(
            onClick = { onMonthChange(selectedMonth.plusMonths(1)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.stats_next_month),
            )
        }
    }

    if (showPicker) {
        StatsMonthPickerDialog(
            selectedMonth = selectedMonth,
            onMonthSelected = {
                onMonthChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun StatsMonthPickerDialog(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit,
) {
    var yearText by remember(selectedMonth) { mutableStateOf(selectedMonth.year.toString()) }
    val pickerYear = yearText.toIntOrNull()
        ?.takeIf { it in 1..9999 }
        ?: selectedMonth.year

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.stats_choose_month)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { yearText = (pickerYear - 1).coerceAtLeast(1).toString() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = stringResource(R.string.stats_previous_year),
                        )
                    }
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { value ->
                            if (value.length <= 4 && value.all(Char::isDigit)) {
                                yearText = value
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text(text = stringResource(R.string.stats_year)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    IconButton(
                        onClick = { yearText = (pickerYear + 1).coerceAtMost(9999).toString() },
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = stringResource(R.string.stats_next_year),
                        )
                    }
                }

                (1..12).chunked(3).forEach { months ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        months.forEach { month ->
                            TextButton(
                                onClick = {
                                    onMonthSelected(YearMonth.of(pickerYear, month))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = if (
                                        pickerYear == selectedMonth.year &&
                                            month == selectedMonth.monthValue
                                    ) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                ),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.stats_month_number, month),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
