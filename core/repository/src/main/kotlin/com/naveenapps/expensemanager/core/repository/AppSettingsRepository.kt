package com.naveenapps.expensemanager.core.repository

/**
 * Provides static app links and contact details without requiring a network service.
 */
interface AppSettingsRepository {

    fun getPrivacyURL(): String

    fun getTermsURL(): String

    fun getAboutUsURL(): String

    fun getGithubURL(): String

    fun getInstagramURL(): String

    fun getTwitterURL(): String

    fun getFeedbackEmail(): String
}
