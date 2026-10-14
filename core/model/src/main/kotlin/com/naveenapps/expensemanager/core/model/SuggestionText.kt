package com.naveenapps.expensemanager.core.model

import java.text.Normalizer
import java.util.Locale

/** Normalizes local suggestion input without sending or persisting the raw note. */
fun normalizeSuggestionText(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFKC)
        .lowercase(Locale.ROOT)
        .replace(Regex("[\\p{P}\\p{S}]+"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()

/**
 * Restricts contains-learning to short, brand-like phrases. Long sentences are exact-only so
 * filler words such as "今天" or "朋友" never become learned categories.
 */
fun isLikelyStableSuggestionKeyword(text: String): Boolean {
    val normalized = normalizeSuggestionText(text)
    if (normalized.length !in 2..8 || normalized.any(Char::isWhitespace)) return false
    val sentenceWords = listOf("今天", "朋友", "出去", "买了", "给", "那个", "东西", "和")
    return sentenceWords.none(normalized::contains)
}
