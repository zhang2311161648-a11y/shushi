package com.naveenapps.expensemanager.ui

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import com.naveenapps.expensemanager.R

enum class HomeScreenBottomBarItems(
    @StringRes val labelResourceID: Int,
    val icon: ImageVector,
) {
    Details(R.string.jiangji_details, Icons.Outlined.ReceiptLong),
    Stats(R.string.jiangji_stats, Icons.Outlined.Insights),
    Me(R.string.jiangji_me, Icons.Outlined.Person),
    Assets(R.string.jiangji_assets, Icons.Outlined.AccountBalanceWallet);

    val id: String
        get() = name.lowercase()

    companion object {
        fun fromPersistedOrder(ids: List<String>): List<HomeScreenBottomBarItems> {
            val byId = entries.associateBy { it.id }
            val persisted = ids.mapNotNull { byId[it.lowercase()] }
            return (persisted + entries).distinct()
        }
    }
}
