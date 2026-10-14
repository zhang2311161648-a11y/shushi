package com.naveenapps.expensemanager.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.naveenapps.expensemanager.core.designsystem.ui.components.AppCardView
import com.naveenapps.expensemanager.core.designsystem.ui.components.ExpenseManagerTopAppBar
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationOrderScreen(
    viewModel: NavigationOrderViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            ExpenseManagerTopAppBar(
                title = stringResource(R.string.navigation_order),
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationBackClick = viewModel::close,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.navigation_order_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            item {
                AppCardView {
                    state.items.forEachIndexed { index, item ->
                        NavigationOrderRow(
                            item = item,
                            isFirst = index == 0,
                            isLast = index == state.items.lastIndex,
                            onMoveUp = { viewModel.move(item, -1) },
                            onMoveDown = { viewModel.move(item, 1) },
                        )
                    }
                }
            }
            item {
                Button(
                    onClick = viewModel::reset,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.navigation_order_reset))
                }
            }
        }
    }
}

@Composable
private fun NavigationOrderRow(
    item: NavigationOrderItem,
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.DragHandle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = navigationOrderTitle(item),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
        IconButton(onClick = onMoveUp, enabled = !isFirst) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.navigation_move_up),
            )
        }
        IconButton(onClick = onMoveDown, enabled = !isLast) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.navigation_move_down),
            )
        }
    }
}

@Composable
private fun navigationOrderTitle(item: NavigationOrderItem): String = when (item) {
    NavigationOrderItem.DETAILS -> stringResource(R.string.navigation_details)
    NavigationOrderItem.STATS -> stringResource(R.string.navigation_stats)
    NavigationOrderItem.ME -> stringResource(R.string.navigation_me)
    NavigationOrderItem.ASSETS -> stringResource(R.string.navigation_assets)
}
