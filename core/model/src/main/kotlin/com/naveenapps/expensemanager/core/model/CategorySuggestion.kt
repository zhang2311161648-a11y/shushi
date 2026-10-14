package com.naveenapps.expensemanager.core.model

/** Where a category suggestion came from. User rules always outrank system rules. */
enum class CategorySuggestionRuleSource {
    SYSTEM,
    USER_LEARNED,
}

/** How a local rule matches the normalized note text. */
enum class CategorySuggestionMatchType {
    EXACT,
    CONTAINS,
}

data class CategorySuggestionRule(
    val id: Long = 0L,
    val pattern: String,
    val normalizedPattern: String,
    val categoryId: String,
    val matchType: CategorySuggestionMatchType,
    val source: CategorySuggestionRuleSource = CategorySuggestionRuleSource.USER_LEARNED,
    val usageCount: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val lastUsedAt: Long,
)

data class CategorySuggestion(
    val categoryId: String,
    val confidence: Double,
    val source: CategorySuggestionRuleSource,
    val matchedPattern: String,
)
