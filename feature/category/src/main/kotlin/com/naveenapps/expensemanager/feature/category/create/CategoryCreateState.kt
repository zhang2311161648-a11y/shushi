package com.naveenapps.expensemanager.feature.category.create

import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.TextFieldValue

data class CategoryCreateState(
    val name: TextFieldValue<String>,
    val color: TextFieldValue<String>,
    val icon: TextFieldValue<String>,
    val type: TextFieldValue<CategoryType>,
    val showDeleteButton: Boolean,
    val showDeleteDialog: Boolean,
    // Set only when editing one of the built-in default categories (see Category.titleResId).
    // The field starts with the localized default name, but can be changed by the user.
    val nameResId: Int? = null,
    // A default category keeps its stable marker while it is only being styled. Once the user
    // changes its name, the marker is cleared on save so every screen displays the custom name.
    val nameChanged: Boolean = false,
    // Absolute path to a user-picked/captured photo (see StoredIcon.customImagePath). When set,
    // the Appearance section shows this photo instead of the icon+color combination above.
    val customImagePath: String? = null,
)

val CategoryCreateState.isDefaultCategory: Boolean get() = nameResId != null
