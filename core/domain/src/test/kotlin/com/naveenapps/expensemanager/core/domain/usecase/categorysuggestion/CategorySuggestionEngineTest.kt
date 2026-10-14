package com.naveenapps.expensemanager.core.domain.usecase.categorysuggestion

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.Category
import com.naveenapps.expensemanager.core.model.CategorySuggestionMatchType
import com.naveenapps.expensemanager.core.model.CategorySuggestionRule
import com.naveenapps.expensemanager.core.model.CategorySuggestionRuleSource
import com.naveenapps.expensemanager.core.model.CategoryType
import com.naveenapps.expensemanager.core.model.StoredIcon
import com.naveenapps.expensemanager.core.model.normalizeSuggestionText
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date

class CategorySuggestionEngineTest {

    private val food = category("food", "餐饮", "food")
    private val beverages = category("beverages", "奶茶咖啡", "beverages")
    private val shopping = category("shopping", "购物", "shopping")
    private val transport = category("transport", "交通", "transportation")

    private val engine = CategorySuggestionEngine().also {
        it.updateActiveCategories(listOf(food, beverages, shopping, transport))
    }

    @Test
    fun systemExactMatchSuggestsBeverages() = runTest {
        val result = engine.suggest("瑞幸")

        assertThat(result.first().categoryId).isEqualTo(beverages.id)
        assertThat(result.first().confidence).isEqualTo(0.90)
        assertThat(result.first().source).isEqualTo(CategorySuggestionRuleSource.SYSTEM)
    }

    @Test
    fun systemContainsMatchSuggestsBeverages() = runTest {
        assertThat(engine.suggest("湛江万达瑞幸咖啡").first().categoryId)
            .isEqualTo(beverages.id)
    }

    @Test
    fun systemRulesRecognizeFoodAndTransport() = runTest {
        assertThat(engine.suggest("麦当劳").first().categoryId).isEqualTo(food.id)
        assertThat(engine.suggest("滴滴回宿舍").first().categoryId).isEqualTo(transport.id)
    }

    @Test
    fun unknownTextDoesNotGuess() = runTest {
        assertThat(engine.suggest("泡泡玛特")).isEmpty()
    }

    @Test
    fun learnedContainsRuleSuggestsUnknownBrand() = runTest {
        engine.updateUserRules(
            listOf(
                learnedRule(
                    pattern = "泡泡玛特",
                    categoryId = shopping.id,
                    matchType = CategorySuggestionMatchType.CONTAINS,
                ),
            ),
        )

        val result = engine.suggest("泡泡玛特盲盒")

        assertThat(result.first().categoryId).isEqualTo(shopping.id)
        assertThat(result.first().source).isEqualTo(CategorySuggestionRuleSource.USER_LEARNED)
        assertThat(result.first().confidence).isEqualTo(0.95)
    }

    @Test
    fun userRuleOverridesSystemRule() = runTest {
        engine.updateUserRules(
            listOf(
                learnedRule(
                    pattern = "山姆",
                    categoryId = food.id,
                    matchType = CategorySuggestionMatchType.CONTAINS,
                ),
            ),
        )

        val result = engine.suggest("山姆买菜")

        assertThat(result.first().categoryId).isEqualTo(food.id)
        assertThat(result.first().source).isEqualTo(CategorySuggestionRuleSource.USER_LEARNED)
    }

    @Test
    fun archivedCategoryIsIgnoredWhenItIsNotActive() = runTest {
        engine.updateUserRules(
            listOf(
                learnedRule(
                    pattern = "泡泡玛特",
                    categoryId = shopping.id,
                    matchType = CategorySuggestionMatchType.CONTAINS,
                ),
            ),
        )
        engine.updateActiveCategories(listOf(food, beverages, transport))

        assertThat(engine.suggest("泡泡玛特")).isEmpty()
    }

    @Test
    fun normalizationHandlesFullWidthTextPunctuationAndCase() {
        assertThat(normalizeSuggestionText("  瑞幸 咖啡！！ ")).isEqualTo("瑞幸 咖啡")
        assertThat(normalizeSuggestionText("  VISA--Cafe ")).isEqualTo("visa cafe")
    }

    @Test
    fun systemExactBeatsSystemContainsForSameCategory() = runTest {
        val result = engine.suggest("瑞幸")

        assertThat(result.first().matchedPattern).isEqualTo("瑞幸")
    }

    @Test
    fun disabledRecommendationCanBeRepresentedByEmptyEngineInput() = runTest {
        // The setting is enforced by the ViewModel; the engine itself remains deterministic and
        // side-effect free so it is safe to use in tests and in offline mode.
        assertThat(engine.suggest("")).isEmpty()
    }

    private fun learnedRule(
        pattern: String,
        categoryId: String,
        matchType: CategorySuggestionMatchType,
    ) = CategorySuggestionRule(
        pattern = pattern,
        normalizedPattern = normalizeSuggestionText(pattern),
        categoryId = categoryId,
        matchType = matchType,
        createdAt = 1L,
        updatedAt = 1L,
        lastUsedAt = 1L,
    )

    private fun category(id: String, name: String, key: String) = Category(
        id = id,
        name = name,
        type = CategoryType.EXPENSE,
        storedIcon = StoredIcon(name = "category", backgroundColor = "#6F1D2C"),
        createdOn = Date(1L),
        updatedOn = Date(1L),
        defaultCategoryKey = key,
    )
}
