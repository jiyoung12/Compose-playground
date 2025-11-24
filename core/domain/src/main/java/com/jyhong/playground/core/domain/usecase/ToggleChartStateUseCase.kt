package com.jyhong.playground.core.domain.usecase

import com.jyhong.playground.core.domain.repository.ChartRepository
import com.jyhong.playground.core.model.ChartType
import javax.inject.Inject

class ToggleChartPlayStateUseCase @Inject constructor(
    private val repository: ChartRepository
) {
    suspend operator fun invoke(type: ChartType, isPlaying: Boolean) {
        repository.setPlayState(type, isPlaying)
    }
}