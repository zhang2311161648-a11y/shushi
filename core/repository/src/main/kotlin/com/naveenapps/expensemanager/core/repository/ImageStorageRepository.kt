package com.naveenapps.expensemanager.core.repository

import android.net.Uri

/**
 * Copies a user-picked (gallery) or captured (camera) photo into app-private storage so it
 * survives independently of whatever content:// permission the picker/camera Uri briefly grants,
 * and hands back a plain absolute file path suitable for persisting on a [Category]/`Account`'s
 * `StoredIcon.customImagePath`.
 */
interface ImageStorageRepository {

    /**
     * A fresh content:// Uri (backed by a private cache file) for the camera app to write a full
     * -resolution capture into. Call this right before launching
     * `ActivityResultContracts.TakePicture()`, then pass the same Uri to [saveCategoryImage] once
     * the capture succeeds.
     */
    fun createImageCaptureUri(): Uri

    /**
     * Downsamples, corrects orientation, compresses, and copies whatever [sourceUri] points to
     * (a gallery pick or a just-captured photo) into a new file under app-private storage.
     * Returns the resulting absolute file path, or null if the image couldn't be read/decoded.
     */
    suspend fun saveCategoryImage(sourceUri: Uri): String?

    /** Best-effort delete of a previously saved custom category image; safe to call with any path. */
    fun deleteCategoryImage(path: String)

    /** Same as [saveCategoryImage], but stored under the account images directory. */
    suspend fun saveAccountImage(sourceUri: Uri): String?

    /** Best-effort delete of a previously saved custom account image; safe to call with any path. */
    fun deleteAccountImage(path: String)

    /** Same as [saveCategoryImage], but stored under the local profile avatar directory. */
    suspend fun saveProfileAvatar(sourceUri: Uri): String?

    /** Best-effort delete of a previously saved local profile avatar. */
    fun deleteProfileAvatar(path: String)

    /**
     * Same as [saveCategoryImage], but stored under the transaction attachments directory. A
     * transaction can have any number of these (unlike Category/Account's single photo), so this
     * is called once per picked/captured photo.
     */
    suspend fun saveTransactionAttachment(sourceUri: Uri): String?

    /** Best-effort delete of a previously saved transaction attachment; safe to call with any path. */
    fun deleteTransactionAttachment(path: String)
}
