package com.naveenapps.expensemanager.feature.analysis

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppDatePickerDialog
import java.time.LocalDate

@Composable
internal fun StatsWeekSelector(
    selectedWeekStart: LocalDate,
    onWeekChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { onWeekChange(selectedWeekStart.minusWeeks(1)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = stringResource(R.string.stats_previous_week),
            )
        }
        TextButton(
            onClick = { showPicker = true },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.small,
        ) {
            AnimatedContent(
                targetState = selectedWeekStart,
                label = "selected_stats_week",
            ) { start ->
                val end = start.plusDays(6)
                Text(
                    text = stringResource(
                        R.string.stats_selected_week,
                        start.year,
                        start.monthValue,
                        start.dayOfMonth,
                        end.year,
                        end.monthValue,
                        end.dayOfMonth,
                    ),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        IconButton(
            onClick = { onWeekChange(selectedWeekStart.plusWeeks(1)) },
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.stats_next_week),
            )
        }
    }

    if (showPicker) {
        AppDatePickerDialog(
            selectedDate = selectedWeekStart.toDateAtStartOfDay(),
            onDateSelected = { selectedDate ->
                onWeekChange(currentCalendarWeekStart(selectedDate.toLocalDateAtStartOfDay()))
                showPicker = false
            },
            onDismiss = { showPicker = false },
        )
    }
}
