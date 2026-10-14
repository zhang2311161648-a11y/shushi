package com.naveenapps.expensemanager.core.domain.usecase.anxietyrelief

import com.naveenapps.expensemanager.core.model.AnxietyReliefItem
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.AnxietyReliefRepository
import com.naveenapps.expensemanager.core.repository.AnxietyReliefSettingsRepository
import kotlinx.coroutines.flow.Flow

class GetAnxietyReliefItemsUseCase(private val repository: AnxietyReliefRepository) {
    operator fun invoke(): Flow<List<AnxietyReliefItem>> = repository.getItems()
}

class AddAnxietyReliefItemUseCase(private val repository: AnxietyReliefRepository) {
    suspend operator fun invoke(item: AnxietyReliefItem): Resource<Boolean> =
        if (item.name.isBlank() || item.priceCents <= 0L || item.targetDailyCostCents <= 0L) {
            Resource.Error(IllegalArgumentException("A name, positive price and target are required"))
        } else {
            repository.add(item)
        }
}

class UpdateAnxietyReliefItemUseCase(private val repository: AnxietyReliefRepository) {
    suspend operator fun invoke(item: AnxietyReliefItem): Resource<Boolean> =
        if (item.name.isBlank() || item.priceCents <= 0L || item.targetDailyCostCents <= 0L) {
            Resource.Error(IllegalArgumentException("A name, positive price and target are required"))
        } else {
            repository.update(item)
        }
}

class ArchiveAnxietyReliefItemUseCase(private val repository: AnxietyReliefRepository) {
    suspend operator fun invoke(item: AnxietyReliefItem): Resource<Boolean> = repository.archive(item)
}

class DeleteAnxietyReliefItemUseCase(private val repository: AnxietyReliefRepository) {
    suspend operator fun invoke(item: AnxietyReliefItem): Resource<Boolean> = repository.delete(item)
}

class GetAnxietyReliefDefaultTargetUseCase(
    private val repository: AnxietyReliefSettingsRepository,
) {
    operator fun invoke(): Flow<Long> = repository.getDefaultDailyTargetCents()
}

class SetAnxietyReliefDefaultTargetUseCase(
    private val repository: AnxietyReliefSettingsRepository,
) {
    suspend operator fun invoke(value: Long): Resource<Boolean> = if (value <= 0L) {
        Resource.Error(IllegalArgumentException("The target must be positive"))
    } else {
        repository.setDefaultDailyTargetCents(value)
    }
}
