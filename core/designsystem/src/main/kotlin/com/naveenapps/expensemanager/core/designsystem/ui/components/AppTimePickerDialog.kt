package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.naveenapps.designsystem.theme.NaveenAppsPreviewTheme
import com.naveenapps.expensemanager.core.designsystem.R


@Composable
fun AppTimePickerDialog(
    reminderTimeState: Triple<Int, Int, Boolean>,
    onTimeSelected: (Triple<Int, Int, Boolean>) -> Unit,
    onDismiss: () -> Unit,
) {
    var timeText by remember(reminderTimeState.first, reminderTimeState.second) {
        val formattedTime = formatTime(reminderTimeState.first, reminderTimeState.second)
        mutableStateOf(
            TextFieldValue(
                text = formattedTime,
                selection = TextRange(formattedTime.length),
            ),
        )
    }
    var hasError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        AppCardView(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.wrapContentSize()) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    value = timeText,
                    onValueChange = { value ->
                        timeText = normalizeTimeInput(value)
                        hasError = false
                    },
                    singleLine = true,
                    isError = hasError,
                    label = { Text(text = "HH:mm") },
                    placeholder = { Text(text = "09:30") },
                    supportingText = if (hasError) {
                        { Text(text = "请输入 00:00 至 23:59") }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(id = R.string.cancel).uppercase())
                    }
                    TextButton(onClick = {
                        val parsedTime = parseTime(timeText.text)
                        if (parsedTime == null) {
                            hasError = true
                        } else {
                            onTimeSelected.invoke(
                                Triple(parsedTime.first, parsedTime.second, reminderTimeState.third),
                            )
                        }
                    }) {
                        Text(text = stringResource(id = R.string.select).uppercase())
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour.coerceIn(0, 23), minute.coerceIn(0, 59))

internal fun normalizeTimeInput(value: TextFieldValue): TextFieldValue {
    val digits = value.text.filter(Char::isDigit).take(4)
    val normalizedText = when {
        digits.length <= 2 -> digits
        else -> digits.substring(0, 2) + ":" + digits.substring(2)
    }

    fun mapCursor(cursor: Int): Int {
        val boundedCursor = cursor.coerceIn(0, value.text.length)
        val digitIndex = value.text.take(boundedCursor).count(Char::isDigit)
        val colonWasInsertedOrKept = digits.length > 2 && boundedCursor > 2
        return (
            digitIndex + if (colonWasInsertedOrKept) 1 else 0
        ).coerceIn(0, normalizedText.length)
    }

    return value.copy(
        text = normalizedText,
        selection = TextRange(
            start = mapCursor(value.selection.start),
            end = mapCursor(value.selection.end),
        ),
    )
}

internal fun parseTime(value: String): Pair<Int, Int>? {
    val match = Regex("^(\\d{2}):(\\d{2})$").matchEntire(value) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    return if (hour in 0..23 && minute in 0..59) hour to minute else null
}

@Composable
@Preview
fun AppTimePickerDialogPreview() {
    NaveenAppsPreviewTheme {
        AppTimePickerDialog(
            reminderTimeState = Triple(10, 0, false),
            onTimeSelected = {},
        ) {}
    }
}
