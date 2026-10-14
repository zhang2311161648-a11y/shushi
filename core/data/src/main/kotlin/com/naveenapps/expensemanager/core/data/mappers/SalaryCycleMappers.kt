package com.naveenapps.expensemanager.core.data.mappers

import com.naveenapps.expensemanager.core.database.entity.SalaryCycleEntity
import com.naveenapps.expensemanager.core.model.SalaryCycle
import java.time.LocalDate

fun SalaryCycleEntity.toDomain(): SalaryCycle? = runCatching {
    SalaryCycle(
        id = id,
        startDate = LocalDate.parse(startDate),
        note = note,
        createdAt = createdAt,
    )
}.getOrNull()

fun SalaryCycle.toEntity(): SalaryCycleEntity = SalaryCycleEntity(
    id = id,
    startDate = startDate.toString(),
    note = note,
    createdAt = createdAt,
)
