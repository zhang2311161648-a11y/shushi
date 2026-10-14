package com.naveenapps.expensemanager.core.model

/** Local-only identity shown in the ShuShi "My space" header. */
data class UserProfile(
    val displayName: String = DEFAULT_PROFILE_NAME,
    /** Absolute path to the app-private avatar image, or null when using the fallback icon. */
    val avatarPath: String? = null,
)

const val DEFAULT_PROFILE_NAME = "我的空间"
