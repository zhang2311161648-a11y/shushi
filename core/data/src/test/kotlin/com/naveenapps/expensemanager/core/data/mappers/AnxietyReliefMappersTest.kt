package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.AnxietyReliefItemEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class AnxietyReliefMappersTest {
    @Test
    fun categorySoldStateAndUsageRoundTrip() {
        val entity = item("[mood:开心]相机").copy(category = "摄影", isSold = true, costByUse = true, useCount = 8)
        assertEquals(entity, entity.toDomainModel().toEntityModel())
    }
    private fun item(note: String) = AnxietyReliefItemEntity(
        id = "legacy-item", name = "Old item", imageUri = "files/old-image.jpg", priceCents = 12345,
        purchaseDate = "2025-01-02", targetDailyCostCents = 500, note = note,
        createdAt = 1234, updatedAt = 5678, isArchived = false,
    )

    @Test
    fun legacyPlainNoteAndAllItemDataRemainIntact() {
        val entity = item("旧版本购买备注，包含 [方括号]")
        val domain = entity.toDomainModel()
        assertEquals(entity.note, domain.note)
        assertEquals("未填写", domain.mood)
        assertEquals(entity.id, domain.id)
        assertEquals(entity.imageUri, domain.imageUri)
        assertEquals(entity.priceCents, domain.priceCents)
        assertEquals(entity.purchaseDate, domain.purchaseDate)
        assertEquals(entity.targetDailyCostCents, domain.targetDailyCostCents)
        assertEquals(entity.createdAt, domain.createdAt)
        assertEquals(entity.updatedAt, domain.updatedAt)
        assertEquals(entity.isArchived, domain.isArchived)
    }

    @Test
    fun newMoodPrefixIsReadAndRoundTripsWithoutAccumulatingPrefixes() {
        val entity = item("[mood:开心]原有备注")
        val domain = entity.toDomainModel()
        assertEquals("开心", domain.mood)
        assertEquals("原有备注", domain.note)
        assertEquals(entity, domain.toEntityModel())
        assertEquals(entity, domain.toEntityModel().toDomainModel().toEntityModel())
    }

    @Test
    fun malformedPrefixRemainsOriginalNote() {
        val entity = item("[mood:没有结束标记")
        assertEquals(entity.note, entity.toDomainModel().note)
        assertEquals("未填写", entity.toDomainModel().mood)
    }
}
