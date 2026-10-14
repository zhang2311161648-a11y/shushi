package com.naveenapps.expensemanager.core.domain.usecase.categorysuggestion

import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySuggestion
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule
import com.naveenapps.expensemanager.core.model.CategorySuggestionRuleSource
import com.naveenapps.expensemanager.core.model.normalizeSuggestionText

/**
 * Deterministic, offline category suggestion service. It only compares text with local rules;
 * it never invokes a network client or an AI service.
 */
class CategorySuggestionEngine(
    initialUserRules: List<CategorySuggestionRule> = emptyList(),
) {

    @Volatile
    private var userRules: List<CategorySuggestionRule> = initialUserRules

    @Volatile
    private var activeCategories: List<Category> = emptyList()

    fun updateUserRules(rules: List<CategorySuggestionRule>) {
        userRules = rules
    }

    fun updateActiveCategories(categories: List<Category>) {
        activeCategories = categories
    }

    /** Suggests categories using the latest in-memory category and learned-rule snapshots. */
    suspend fun suggest(text: String): List<CategorySuggestion> =
        suggest(text, activeCategories, userRules)

    /** Explicit-input overload keeps the engine straightforward to unit test. */
    suspend fun suggest(
        text: String,
        categories: List<Category>,
    ): List<CategorySuggestion> = suggest(text, categories, userRules)

    private fun suggest(
        text: String,
        categories: List<Category>,
        rules: List<CategorySuggestionRule>,
    ): List<CategorySuggestion> {
        val normalizedText = normalizeSuggestionText(text)
        if (normalizedText.isBlank() || categories.isEmpty()) return emptyList()

        val activeCategoryIds = categories.asSequence().map { it.id }.toSet()
        val candidates = buildList {
            rules.asSequence()
                .filter { it.source == CategorySuggestionRuleSource.USER_LEARNED }
                .filter { it.categoryId in activeCategoryIds }
                .filter { ruleMatches(it.normalizedPattern, normalizedText, it.matchType) }
                .forEach { rule ->
                    add(
                        RankedSuggestion(
                            suggestion = CategorySuggestion(
                                categoryId = rule.categoryId,
                                confidence = if (normalizedText == rule.normalizedPattern) 1.0 else 0.95,
                                source = CategorySuggestionRuleSource.USER_LEARNED,
                                matchedPattern = rule.pattern,
                            ),
                            sourceRank = 2,
                            exactRank = if (normalizedText == rule.normalizedPattern) 1 else 0,
                            patternLength = rule.normalizedPattern.length,
                            usageCount = rule.usageCount,
                        ),
                    )
                }

            CategoryKeywordRegistry.rules.forEach { rule ->
                val category = categories.firstOrNull { it.matchesBuiltInKey(rule) }
                    ?: return@forEach
                rule.keywords.forEach { keyword ->
                    val normalizedKeyword = normalizeSuggestionText(keyword)
                    if (normalizedKeyword.isBlank() || !normalizedText.contains(normalizedKeyword)) {
                        return@forEach
                    }
                    val exact = normalizedText == normalizedKeyword
                    add(
                        RankedSuggestion(
                            suggestion = CategorySuggestion(
                                categoryId = category.id,
                                confidence = if (exact) 0.90 else 0.80,
                                source = CategorySuggestionRuleSource.SYSTEM,
                                matchedPattern = keyword,
                            ),
                            sourceRank = 1,
                            exactRank = if (exact) 1 else 0,
                            patternLength = normalizedKeyword.length,
                            usageCount = 0,
                        ),
                    )
                }
            }
        }

        return candidates
            .groupBy { it.suggestion.categoryId }
            .values
            .mapNotNull { categoryCandidates ->
                categoryCandidates.maxWithOrNull(
                    compareBy<RankedSuggestion> {
                        it.sourceRank
                    }.thenBy { it.exactRank }
                        .thenBy { it.suggestion.confidence }
                        .thenBy { it.patternLength }
                        .thenBy { it.usageCount },
                )
            }
            .sortedWith(
                compareByDescending<RankedSuggestion> { it.sourceRank }
                    .thenByDescending { it.exactRank }
                    .thenByDescending { it.suggestion.confidence }
                    .thenByDescending { it.patternLength }
                    .thenByDescending { it.usageCount }
                    .thenBy { it.suggestion.categoryId },
            )
            .map { it.suggestion }
    }

    private fun ruleMatches(
        normalizedPattern: String,
        normalizedText: String,
        matchType: com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType,
    ): Boolean = when (matchType) {
        com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType.EXACT ->
            normalizedText == normalizedPattern

        com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType.CONTAINS ->
            normalizedText.contains(normalizedPattern)
    }

    private fun Category.matchesBuiltInKey(rule: CategoryKeywordRule): Boolean {
        val normalizedName = normalizeSuggestionText(name)
        return defaultCategoryKey == rule.categoryKey ||
            rule.categoryNameAliases.any { normalizeSuggestionText(it) == normalizedName }
    }

    private data class RankedSuggestion(
        val suggestion: CategorySuggestion,
        val sourceRank: Int,
        val exactRank: Int,
        val patternLength: Int,
        val usageCount: Int,
    )
}
