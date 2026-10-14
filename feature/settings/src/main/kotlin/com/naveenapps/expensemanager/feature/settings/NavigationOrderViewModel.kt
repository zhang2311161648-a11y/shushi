package com.naveenapps.expensemanager.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.navigation.AppComposeNavigator
import com.naveenapps.expensemanager.core.repository.NavigationOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class NavigationOrderItem(val persistedId: String) {
    DETAILS("details"),
    STATS("stats"),
    ME("me"),
    ASSETS("assets");

    companion object {
        val defaultOrder: List<NavigationOrderItem> = entries

        fun fromPersistedOrder(ids: List<String>): List<NavigationOrderItem> {
            val byId = entries.associateBy { it.persistedId }
            val persisted = ids.mapNotNull { byId[it.lowercase()] }
            return (persisted + defaultOrder).distinct()
        }
    }
}

data class NavigationOrderState(
    val items: List<NavigationOrderItem> = NavigationOrderItem.defaultOrder,
)

class NavigationOrderViewModel(
    private val navigationOrderRepository: NavigationOrderRepository,
    private val appComposeNavigator: AppComposeNavigator,
) : ViewModel() {

    private val _state = MutableStateFlow(NavigationOrderState())
    val state = _state.asStateFlow()

    init {
        navigationOrderRepository.observeBottomBarOrder()
            .onEach { ids ->
                _state.value = NavigationOrderState(NavigationOrderItem.fromPersistedOrder(ids))
            }
            .launchIn(viewModelScope)
    }

    fun move(item: NavigationOrderItem, offset: Int) {
        val current = _state.value.items
        val fromIndex = current.indexOf(item)
        val toIndex = (fromIndex + offset).coerceIn(0, current.lastIndex)
        if (fromIndex < 0 || fromIndex == toIndex) return

        val next = current.toMutableList().apply {
            add(toIndex, removeAt(fromIndex))
        }
        _state.value = NavigationOrderState(next)
        persist(next)
    }

    fun reset() {
        _state.value = NavigationOrderState(NavigationOrderItem.defaultOrder)
        viewModelScope.launch { navigationOrderRepository.resetBottomBarOrder() }
    }

    fun close() {
        appComposeNavigator.popBackStack()
    }

    private fun persist(items: List<NavigationOrderItem>) {
        viewModelScope.launch {
            navigationOrderRepository.saveBottomBarOrder(items.map { it.persistedId })
        }
    }
}
