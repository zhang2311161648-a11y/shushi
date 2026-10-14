package com.naveenapps.expensemanager.core.domain.usecase.monthlysaving

import com.naveenapps.expensemanager.core.model.MonthlySaving
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.MonthlySavingRepository
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.math.RoundingMode

class GetMonthlySavingUseCase(private val repository: MonthlySavingRepository) {
    suspend operator fun invoke(year: Int, month: Int): MonthlySaving? = repository.getSaving(year, month)

    suspend operator fun invoke(periodKey: String): MonthlySaving? = repository.getSaving(periodKey)
}

class ObserveMonthlySavingUseCase(private val repository: MonthlySavingRepository) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlySaving?> = repository.observeSaving(year, month)

    operator fun invoke(periodKey: String): Flow<MonthlySaving?> = repository.observeSaving(periodKey)
}

class ObserveMonthlySavingsByYearUseCase(private val repository: MonthlySavingRepository) {
    operator fun invoke(year: Int): Flow<List<MonthlySaving>> = repository.observeSavingsByYear(year)
}

class GetMonthlySavingsYearTotalUseCase(private val repository: MonthlySavingRepository) {
    operator fun invoke(year: Int): Flow<Long> = repository.getYearTotal(year)
}

class UpsertMonthlySavingUseCase(private val repository: MonthlySavingRepository) {
    suspend operator fun invoke(saving: MonthlySaving): Resource<Boolean> = when {
        saving.year !in 1..9999 -> Resource.Error(IllegalArgumentException("Year is invalid"))
        saving.month !in 1..12 -> Resource.Error(IllegalArgumentException("Month is invalid"))
        saving.amountCents < 0L -> Resource.Error(IllegalArgumentException("Savings cannot be negative"))
        else -> repository.upsertMonthlySaving(saving)
    }
}

class DeleteMonthlySavingUseCase(private val repository: MonthlySavingRepository) {
    suspend operator fun invoke(saving: MonthlySaving): Resource<Boolean> = repository.deleteMonthlySaving(saving)
}

/** Parses user input directly into cents without using floating-point arithmetic. */
fun parseMonthlySavingAmountCents(raw: String): Long? {
    val trimmed = raw.trim()
    val normalized = when {
        trimmed.contains(',') && trimmed.contains('.') -> trimmed.replace(",", "")
        trimmed.count { it == ',' } == 1 && trimmed.substringAfter(',').length == 3 -> trimmed.replace(",", "")
        else -> trimmed.replace(',', '.')
    }
    if (normalized.isBlank()) return null

    return runCatching {
        BigDecimal(normalized)
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()?.takeIf { it >= 0L }
}
