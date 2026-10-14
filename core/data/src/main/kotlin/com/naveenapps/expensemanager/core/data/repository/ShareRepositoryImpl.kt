package com.naveenapps.expensemanager.core.data.repository

import android.content.Context
import com.naveenapps.expensemanager.core.common.utils.openAppSettings
import com.naveenapps.expensemanager.core.common.utils.openEmailOption
import com.naveenapps.expensemanager.core.common.utils.openEmailToOption
import com.naveenapps.expensemanager.core.common.utils.openPrintOption
import com.naveenapps.expensemanager.core.common.utils.openRateUs
import com.naveenapps.expensemanager.core.common.utils.openShareOption
import com.naveenapps.expensemanager.core.common.utils.openWebPage
import com.naveenapps.expensemanager.core.repository.AppSettingsRepository
import com.naveenapps.expensemanager.core.repository.ShareRepository
import java.io.File

class ShareRepositoryImpl(
    val context: Context,
    private val appSettingsRepository: AppSettingsRepository
) : ShareRepository {

    override fun sendEmail(file: File?) {
        file?.let {
            context.openEmailOption(it)
        } ?: run {
            openEmailToOption(context, appSettingsRepository.getFeedbackEmail())
        }
    }

    override fun openFeedbackEmail(message: String) {
        openEmailToOption(
            context = context,
            emailId = FEEDBACK_EMAIL,
            subject = FEEDBACK_SUBJECT,
            body = message,
        )
    }

    override fun share(file: File?) {
        file?.let {
            context.openShareOption(it)
        }
    }

    override fun print(file: File?) {
        file?.let {
            context.openPrintOption(it)
        }
    }

    override fun openRateUs() {
        context.openRateUs()
    }

    override suspend fun requestInAppReview() {
        // The single-resource build is fully local. Do not contact Google Play services.
        Unit
    }

    override fun openPrivacy() {
        context.openWebPage(appSettingsRepository.getPrivacyURL())
    }

    override fun openTerms() {
        context.openWebPage(appSettingsRepository.getTermsURL())
    }

    override fun openAboutUs() {
        context.openWebPage(appSettingsRepository.getAboutUsURL())
    }

    override fun openGithub() {
        context.openWebPage(appSettingsRepository.getGithubURL())
    }

    override fun openInstagram() {
        context.openWebPage(appSettingsRepository.getInstagramURL())
    }

    override fun openTwitter() {
        context.openWebPage(appSettingsRepository.getTwitterURL())
    }

    override fun openAppSettings() {
        context.openAppSettings()
    }

    companion object {
        private const val FEEDBACK_EMAIL = "zhang2311161648@gmail.com"
        private const val FEEDBACK_SUBJECT = "书史用户反馈"
    }
}
