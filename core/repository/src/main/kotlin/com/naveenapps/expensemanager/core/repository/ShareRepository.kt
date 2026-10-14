package com.naveenapps.expensemanager.core.repository

import java.io.File

interface ShareRepository {

    fun sendEmail(file: File?)

    /** Opens an email draft addressed to the ShuShi feedback contact. */
    fun openFeedbackEmail(message: String)

    fun share(file: File?)

    fun print(file: File?)

    fun openRateUs()

    /**
     * Kept for the existing feedback flow. It is a no-op in the local-only build and never
     * contacts an app store or any other network service.
     */
    suspend fun requestInAppReview()

    fun openPrivacy()

    fun openTerms()

    fun openAboutUs()

    fun openGithub()

    fun openInstagram()

    fun openTwitter()

    fun openAppSettings()
}
