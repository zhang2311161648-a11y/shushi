package com.naveenapps.expensemanager.core.data.repository

import com.naveenapps.expensemanager.core.repository.AppSettingsRepository

/**
 * Local-only replacement for the former remote settings provider.
 * Values are compiled into the APK and never fetched over the network.
 */
class LocalSettingsRepositoryImpl : AppSettingsRepository {

    override fun getPrivacyURL(): String = DEFAULT_PRIVACY_URL

    override fun getTermsURL(): String = DEFAULT_TERMS_URL

    override fun getAboutUsURL(): String = DEFAULT_ABOUT_US_URL

    override fun getGithubURL(): String = DEFAULT_GITHUB_URL

    override fun getInstagramURL(): String = DEFAULT_INSTAGRAM_URL

    override fun getTwitterURL(): String = DEFAULT_TWITTER_URL

    override fun getFeedbackEmail(): String = DEFAULT_FEEDBACK_EMAIL

    private companion object {
        const val DEFAULT_PRIVACY_URL = "https://github.com/zhang2311161648-a11y/shushi/blob/main/docs/PRIVACY.md"
        const val DEFAULT_TERMS_URL = "https://github.com/zhang2311161648-a11y/shushi/blob/main/LICENSE"
        const val DEFAULT_ABOUT_US_URL = "https://github.com/zhang2311161648-a11y/shushi"
        const val DEFAULT_GITHUB_URL = "https://github.com/zhang2311161648-a11y/shushi"
        const val DEFAULT_INSTAGRAM_URL = "https://github.com/zhang2311161648-a11y/shushi"
        const val DEFAULT_TWITTER_URL = "https://github.com/zhang2311161648-a11y/shushi"
        const val DEFAULT_FEEDBACK_EMAIL = "zhang2311161648@gmail.com"
    }
}
