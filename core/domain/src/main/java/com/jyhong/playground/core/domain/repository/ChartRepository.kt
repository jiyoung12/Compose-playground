package com.jyhong.playground.core.domain.repository

import com.jyhong.playground.core.model.ChartData
import com.jyhong.playground.core.model.ChartType
import kotlinx.coroutines.flow.Flow

interface ChartRepository {

    fun getChartDataStream(type : ChartType) : Flow<ChartData>

    suspend fun updateDataCount(type : ChartType, count : Int)

    suspend fun setPlayState(type: ChartType, isPlaying: Boolean)
}