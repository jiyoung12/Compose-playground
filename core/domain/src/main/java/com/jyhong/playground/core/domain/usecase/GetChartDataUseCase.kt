package com.jyhong.playground.core.domain.usecase

import com.jyhong.playground.core.domain.repository.ChartRepository
import com.jyhong.playground.core.model.ChartType
import javax.inject.Inject

class GetChartDataUseCase @Inject constructor(private val chartRepository: ChartRepository) {

    operator fun invoke(chartType: ChartType) = chartRepository.getChartDataStream(type = chartType)
}