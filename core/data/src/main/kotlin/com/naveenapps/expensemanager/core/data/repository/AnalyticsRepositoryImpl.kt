package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.repository.AnalyticsRepository

/** Local-only analytics implementation. It intentionally does not persist or transmit events. */
class AnalyticsRepositoryImpl : AnalyticsRepository {

    override fun trackAppOpenEvent() = Unit

    override fun logEvent(eventName: String, params: Map<String, String>) = Unit

    override fun setCurrentScreen(screenName: String) = Unit

    override fun setUserProperties() = Unit
}
