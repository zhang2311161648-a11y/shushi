package com.naveenapps.expensemanager.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naveenapps.expensemanager.core.model.CategoryType
import java.util.Date

@Entity(tableName = "category", indices = [Index(value = ["ledger_id"])])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "type")
    val type: CategoryType,
    @ColumnInfo(name = "icon_background_color")
    val iconBackgroundColor: String,
    @ColumnInfo(name = "icon_name")
    val iconName: String,
    @ColumnInfo(name = "updated_on")
    val updatedOn: Date,
    @ColumnInfo(name = "created_on")
    val createdOn: Date,
    @ColumnInfo(name = "default_category_key", defaultValue = "NULL")
    val defaultCategoryKey: String? = null,
    @ColumnInfo(name = "custom_image_path", defaultValue = "NULL")
    val customImagePath: String? = null,
    @ColumnInfo(name = "ledger_id", defaultValue = "'default'")
    val ledgerId: String = com.naveenapps.expensemanager.core.model.DEFAULT_LEDGER_ID,
)
