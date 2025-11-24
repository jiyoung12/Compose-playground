package com.jyhong.playground.core.data

import com.jyhong.playground.core.data.datasource.FakeChartDataSource
import com.jyhong.playground.core.domain.repository.ChartRepository
import com.jyhong.playground.core.model.ChartData
import com.jyhong.playground.core.model.ChartType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChartRepositoryImpl @Inject constructor(
    private val dataSource: FakeChartDataSource
) : ChartRepository {

    override fun getChartDataStream(type: ChartType): Flow<ChartData> {
        return dataSource.getChartDataFlow(type).map { entries ->
            ChartData(
                title = "${type.name} Live",
                type = type,
                entries = entries
            )
        }
    }

    override suspend fun updateDataCount(type: ChartType, count: Int) {
        dataSource.updateCount(type, count)
    }

    override suspend fun setPlayState(type: ChartType, isPlaying: Boolean) {
        dataSource.setPlayState(type, isPlaying)
    }
}