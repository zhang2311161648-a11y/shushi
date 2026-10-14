package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
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

@Composable
internal fun StatsYearSelector(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onYearChange((selectedYear - 1).coerceAtLeast(1)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.stats_previous_year),
            )
        }
        TextButton(
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
        ) {
            AnimatedContent(
                targetState = selectedYear,
                label = "selected_stats_year",
            ) { year ->
                Text(
                    text = stringResource(R.string.stats_selected_year, year),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        IconButton(
            onClick = { onYearChange((selectedYear + 1).coerceAtMost(9999)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.stats_next_year),
            )
        }
    }

    if (showPicker) {
        StatsYearPickerDialog(
            selectedYear = selectedYear,
            onYearSelected = {
                onYearChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}

@Composable
private fun StatsYearPickerDialog(
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var yearText by remember(selectedYear) { mutableStateOf(selectedYear.toString()) }
    val year = yearText.toIntOrNull()?.takeIf { it in 1..9999 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.stats_choose_year)) },
        text = {
            OutlinedTextField(
                value = yearText,
                onValueChange = { value ->
                    if (value.length <= 4 && value.all(Char::isDigit)) {
                        yearText = value
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(text = stringResource(R.string.stats_year)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { year?.let(onYearSelected) },
                enabled = year != null,
            ) {
                Text(text = stringResource(com.naveenapps.expensemanager.core.designsystem.R.string.select))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
