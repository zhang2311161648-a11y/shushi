package com.naveenapps.expensemanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.repository.NavigationOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class HomeViewModel(
    private val navigationOrderRepository: NavigationOrderRepository,
) : ViewModel() {

    private val _homeScreenBottomBarItems = MutableStateFlow(HomeScreenBottomBarItems.Details)
    val homeScreenBottomBarItems = _homeScreenBottomBarItems.asStateFlow()

    private val _navigationOrder = MutableStateFlow<List<HomeScreenBottomBarItems>>(
        HomeScreenBottomBarItems.entries.toList(),
    )
    val navigationOrder = _navigationOrder.asStateFlow()

    init {
        navigationOrderRepository.observeBottomBarOrder()
            .onEach { persistedIds ->
                _navigationOrder.value = HomeScreenBottomBarItems.fromPersistedOrder(persistedIds)
            }
            .launchIn(viewModelScope)
    }

    fun setUISystem(homeScreenBottomBarItems: HomeScreenBottomBarItems) {
        _homeScreenBottomBarItems.value = homeScreenBottomBarItems
    }

    fun saveNavigationOrder(items: List<HomeScreenBottomBarItems>) {
        val normalized = HomeScreenBottomBarItems.fromPersistedOrder(items.map { it.id })
        _navigationOrder.value = normalized
        viewModelScope.launch {
            navigationOrderRepository.saveBottomBarOrder(normalized.map { it.id })
        }
    }

    fun resetNavigationOrder() {
        _navigationOrder.value = HomeScreenBottomBarItems.entries.toList()
        viewModelScope.launch {
            navigationOrderRepository.resetBottomBarOrder()
        }
    }
}
