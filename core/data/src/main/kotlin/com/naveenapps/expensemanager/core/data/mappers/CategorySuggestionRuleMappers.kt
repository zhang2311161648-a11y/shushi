package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.CategorySuggestionRuleEntity
import com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule
import com.naveenapps.expensemanager.core.model.CategorySuggestionRuleSource

fun CategorySuggestionRuleEntity.toDomainModel(): CategorySuggestionRule? {
    val matchType = CategorySuggestionMatchType.entries.getOrNull(this.matchType)
        ?: return null
    val source = CategorySuggestionRuleSource.entries.getOrNull(this.source)
        ?: return null
    return CategorySuggestionRule(
        id = id,
        pattern = pattern,
        normalizedPattern = normalizedPattern,
        categoryId = categoryId,
        matchType = matchType,
        source = source,
        usageCount = usageCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastUsedAt = lastUsedAt,
    )
}

fun CategorySuggestionRule.toEntityModel(): CategorySuggestionRuleEntity =
    CategorySuggestionRuleEntity(
        id = id,
        pattern = pattern,
        normalizedPattern = normalizedPattern,
        categoryId = categoryId,
        matchType = matchType.ordinal,
        source = source.ordinal,
        usageCount = usageCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastUsedAt = lastUsedAt,
    )
