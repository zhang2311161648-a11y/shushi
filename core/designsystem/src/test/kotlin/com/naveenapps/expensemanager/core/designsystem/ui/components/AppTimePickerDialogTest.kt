package com.naveenapps.expensemanager.core.designsystem.ui.components

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.junit.Assert.assertEquals
import org.junit.Test

class AppTimePickerDialogTest {

    @Test
    fun typing1240Produces1240InsteadOf1204() {
        val result = "1240".fold(TextFieldValue(text = "", selection = TextRange.Zero)) { state, digit ->
            val rawValue = state.copy(
                text = state.text.replaceRange(
                    state.selection.start,
                    state.selection.end,
                    digit.toString(),
                ),
                selection = TextRange(state.selection.start + 1),
            )
            normalizeTimeInput(rawValue)
        }

        assertEquals("12:40", result.text)
    }
}
