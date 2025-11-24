package com.jyhong.playground.core.domain.usecase

import com.jyhong.playground.core.domain.repository.ChartRepository
import com.jyhong.playground.core.model.ChartType
import javax.inject.Inject

class UpdateChartCountUseCase @Inject constructor(
    private val repository: ChartRepository
) {
    suspend operator fun invoke(type: ChartType, count: Int) {
        repository.updateDataCount(type, count)
    }
}