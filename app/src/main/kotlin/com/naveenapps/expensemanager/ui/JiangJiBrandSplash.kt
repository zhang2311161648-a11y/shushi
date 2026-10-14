package com.naveenapps.expensemanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.naveenapps.expensemanager.core.designsystem.ui.components.ShushiBrandLockup

/** A deliberately quiet handoff from the system splash to the first screen. */
@Composable
fun JiangJiBrandSplash(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ShushiBrandLockup(compact = true)
        }
    }
}
