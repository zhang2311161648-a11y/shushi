package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.MonthlySavingEntity
import com.naveenapps.expensemanager.core.model.MonthlySaving
import java.time.LocalDate

fun MonthlySavingEntity.toDomainModel(): MonthlySaving = MonthlySaving(
    id = id,
    year = year,
    month = month,
    amountCents = amountCents,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
    periodKey = periodKey,
    periodStartDate = periodStartDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    periodEndExclusiveDate = periodEndExclusiveDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
)

fun MonthlySaving.toEntityModel(
    idOverride: Long = id,
    createdAtOverride: Long = createdAt,
): MonthlySavingEntity = MonthlySavingEntity(
    id = idOverride,
    year = year,
    month = month,
    amountCents = amountCents,
    note = note,
    createdAt = createdAtOverride,
    updatedAt = updatedAt,
    periodKey = periodKey,
    periodStartDate = periodStartDate?.toString(),
    periodEndExclusiveDate = periodEndExclusiveDate?.toString(),
)
