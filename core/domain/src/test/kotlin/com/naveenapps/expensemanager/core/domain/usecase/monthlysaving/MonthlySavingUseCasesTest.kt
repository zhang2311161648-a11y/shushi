package com.naveenapps.expensemanager.core.domain.usecase.monthlysaving

import com.google.common.truth.Truth.assertThat
import com.naveenapps.expensemanager.core.model.MonthlySaving
import com.naveenapps.expensemanager.core.model.Resource
import com.naveenapps.expensemanager.core.repository.MonthlySavingRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.mock

class MonthlySavingUseCasesTest {

    private val repository: MonthlySavingRepository = mock()

    @Test
    fun `amount parser stores exact cents and accepts zero`() {
        assertThat(parseMonthlySavingAmountCents("1,200.50")).isEqualTo(120_050L)
        assertThat(parseMonthlySavingAmountCents("0")).isEqualTo(0L)
    }

    @Test
    fun `amount parser rejects negative blank and more than two decimals`() {
        assertThat(parseMonthlySavingAmountCents("-1")).isNull()
        assertThat(parseMonthlySavingAmountCents(" ")).isNull()
        assertThat(parseMonthlySavingAmountCents("1.001")).isNull()
    }

    @Test
    fun `upsert rejects invalid month and negative amount before repository`() = runTest {
        val useCase = UpsertMonthlySavingUseCase(repository)

        val invalidMonth = useCase(saving(month = 13))
        val negativeAmount = useCase(saving(amountCents = -1L))

        assertThat(invalidMonth).isInstanceOf(Resource.Error::class.java)
        assertThat(negativeAmount).isInstanceOf(Resource.Error::class.java)
        verify(repository, never()).upsertMonthlySaving(any())
    }

    @Test
    fun `year total is read from monthly savings repository`() = runTest {
        whenever(repository.getYearTotal(2026)).thenReturn(flowOf(410_000L))

        val total = GetMonthlySavingsYearTotalUseCase(repository)(2026).first()

        assertThat(total).isEqualTo(410_000L)
        verify(repository).getYearTotal(2026)
    }

    @Test
    fun `monthly saving is a standalone record passed only to monthly saving repository`() = runTest {
        whenever(repository.upsertMonthlySaving(any())).thenReturn(Resource.Success(true))
        val saving = saving(amountCents = 80_000L)

        val result = UpsertMonthlySavingUseCase(repository)(saving)

        assertThat(result).isEqualTo(Resource.Success(true))
        verify(repository).upsertMonthlySaving(saving)
    }

    private fun saving(
        month: Int = 8,
        amountCents: Long = 80_000L,
    ) = MonthlySaving(
        year = 2026,
        month = month,
        amountCents = amountCents,
        createdAt = 1L,
        updatedAt = 1L,
    )
}
