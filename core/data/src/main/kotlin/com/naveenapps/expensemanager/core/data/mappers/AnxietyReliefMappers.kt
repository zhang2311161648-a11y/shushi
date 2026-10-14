package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.AnxietyReliefItemEntity
import com.naveenapps.expensemanager.core.model.AnxietyReliefItem

private fun parseMoodFromNote(note: String): Pair<String, String> {
    if (note.startsWith("[mood:") && note.contains("]")) {
        val end = note.indexOf("]")
        val mood = note.substring(6, end)
        val clean = note.substring(end + 1)
        return mood to clean
    }
    return "未填写" to note
}

fun AnxietyReliefItem.toEntityModel() = AnxietyReliefItemEntity(
    id = id,
    name = name,
    imageUri = imageUri,
    priceCents = priceCents,
    purchaseDate = purchaseDate,
    targetDailyCostCents = if (targetDailyCostCents <= 0L) 100L else targetDailyCostCents,
    note = if (mood.isNotBlank()) "[mood:$mood]$note" else note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isArchived = isArchived,
    category = category,
    isSold = isSold,
    costByUse = costByUse,
    useCount = useCount,
)

fun AnxietyReliefItemEntity.toDomainModel(): AnxietyReliefItem {
    val (parsedMood, cleanNote) = parseMoodFromNote(note)
    return AnxietyReliefItem(
        id = id,
        name = name,
        imageUri = imageUri,
        priceCents = priceCents,
        purchaseDate = purchaseDate,
        targetDailyCostCents = targetDailyCostCents,
        note = cleanNote,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isArchived = isArchived,
        mood = parsedMood,
        category = category,
        isSold = isSold,
        costByUse = costByUse,
        useCount = useCount,
    )
}
