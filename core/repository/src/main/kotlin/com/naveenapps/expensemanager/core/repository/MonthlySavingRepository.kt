package com.naveenapps.expensemanager.core.repository

import com.naveenapps.expensemanager.core.model.MonthlySaving
import com.naveenapps.expensemanager.core.model.Resource
import kotlinx.coroutines.flow.Flow

interface MonthlySavingRepository {

    suspend fun getSaving(year: Int, month: Int): MonthlySaving?

    fun observeSaving(year: Int, month: Int): Flow<MonthlySaving?>

    suspend fun getSaving(periodKey: String): MonthlySaving?

    fun observeSaving(periodKey: String): Flow<MonthlySaving?>

    fun observeSavingsByYear(year: Int): Flow<List<MonthlySaving>>

    fun getYearTotal(year: Int): Flow<Long>

    suspend fun upsertMonthlySaving(saving: MonthlySaving): Resource<Boolean>

    suspend fun deleteMonthlySaving(saving: MonthlySaving): Resource<Boolean>
}
