package com.naveenapps.expensemanager.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naveenapps.expensemanager.core.domain.usecase.profile.ObserveUserProfileUseCase
import com.naveenapps.expensemanager.core.domain.usecase.profile.SaveUserProfileUseCase
import com.naveenapps.expensemanager.core.model.DEFAULT_PROFILE_NAME
import com.naveenapps.expensemanager.core.model.UserProfile
import com.naveenapps.expensemanager.core.repository.ImageStorageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JiangJiProfileState(
    val profile: UserProfile = UserProfile(),
    val isEditorVisible: Boolean = false,
    val draftName: String = DEFAULT_PROFILE_NAME,
    val draftAvatarPath: String? = null,
    val isAvatarLoading: Boolean = false,
    val isSaving: Boolean = false,
    val validationError: String? = null,
    val avatarError: String? = null,
    val saveError: String? = null,
)

class JiangJiProfileViewModel(
    observeUserProfile: ObserveUserProfileUseCase,
    private val saveUserProfile: SaveUserProfileUseCase,
    private val imageStorageRepository: ImageStorageRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(JiangJiProfileState())
    val state = _state.asStateFlow()

    private val stagedAvatarPaths = linkedSetOf<String>()

    init {
        observeUserProfile()
            .onEach { profile ->
                _state.update { current ->
                    current.copy(
                        profile = profile,
                        draftName = if (current.isEditorVisible) current.draftName else profile.displayName,
                        draftAvatarPath = if (current.isEditorVisible) {
                            current.draftAvatarPath
                        } else {
                            profile.avatarPath
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun openEditor() {
        val profile = _state.value.profile
        _state.update {
            it.copy(
                isEditorVisible = true,
                draftName = profile.displayName,
                draftAvatarPath = profile.avatarPath,
                validationError = null,
                avatarError = null,
                saveError = null,
            )
        }
    }

    fun dismissEditor() {
        discardStagedAvatars()
        val profile = _state.value.profile
        _state.update {
            it.copy(
                isEditorVisible = false,
                draftName = profile.displayName,
                draftAvatarPath = profile.avatarPath,
                isAvatarLoading = false,
                isSaving = false,
                validationError = null,
                avatarError = null,
                saveError = null,
            )
        }
    }

    fun updateDraftName(name: String) {
        _state.update {
            it.copy(
                draftName = name.take(MAX_DISPLAY_NAME_LENGTH),
                validationError = null,
                saveError = null,
            )
        }
    }

    fun onAvatarSelected(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isAvatarLoading = true, avatarError = null) }
            try {
                val newPath = imageStorageRepository.saveProfileAvatar(uri)
                if (newPath == null) {
                    _state.update { it.copy(isAvatarLoading = false, avatarError = AVATAR_ERROR) }
                    return@launch
                }

                val previousDraftPath = _state.value.draftAvatarPath
                if (previousDraftPath != null && stagedAvatarPaths.remove(previousDraftPath)) {
                    imageStorageRepository.deleteProfileAvatar(previousDraftPath)
                }
                stagedAvatarPaths += newPath
                _state.update {
                    it.copy(
                        draftAvatarPath = newPath,
                        isAvatarLoading = false,
                        avatarError = null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(isAvatarLoading = false, avatarError = AVATAR_ERROR) }
            }
        }
    }

    fun removeDraftAvatar() {
        val path = _state.value.draftAvatarPath
        if (path != null && stagedAvatarPaths.remove(path)) {
            imageStorageRepository.deleteProfileAvatar(path)
        }
        _state.update { it.copy(draftAvatarPath = null, avatarError = null) }
    }

    fun save() {
        val current = _state.value
        val displayName = current.draftName.trim()
        if (displayName.isEmpty()) {
            _state.update { it.copy(validationError = NAME_REQUIRED_ERROR) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, validationError = null, saveError = null) }
            try {
                saveUserProfile(
                    UserProfile(
                        displayName = displayName,
                        avatarPath = current.draftAvatarPath,
                    ),
                )

                val previousAvatarPath = current.profile.avatarPath
                if (previousAvatarPath != null && previousAvatarPath != current.draftAvatarPath) {
                    imageStorageRepository.deleteProfileAvatar(previousAvatarPath)
                }
                stagedAvatarPaths.remove(current.draftAvatarPath)
                discardStagedAvatars()
                _state.update {
                    it.copy(
                        isEditorVisible = false,
                        isSaving = false,
                        isAvatarLoading = false,
                        profile = UserProfile(displayName, current.draftAvatarPath),
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _state.update { it.copy(isSaving = false, saveError = SAVE_ERROR) }
            }
        }
    }

    private fun discardStagedAvatars() {
        stagedAvatarPaths.toList().forEach(imageStorageRepository::deleteProfileAvatar)
        stagedAvatarPaths.clear()
    }

    override fun onCleared() {
        discardStagedAvatars()
        super.onCleared()
    }

    private companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 32
        const val NAME_REQUIRED_ERROR = "请输入用户名"
        const val AVATAR_ERROR = "头像读取失败，请换一张图片"
        const val SAVE_ERROR = "保存失败，请重试"
    }
}
