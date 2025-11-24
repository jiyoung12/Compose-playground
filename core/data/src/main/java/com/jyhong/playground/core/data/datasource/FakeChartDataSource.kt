package com.jyhong.playground.core.data.datasource

import com.jyhong.playground.core.model.BarEntry
import com.jyhong.playground.core.model.ChartEntry
import com.jyhong.playground.core.model.ChartType
import com.jyhong.playground.core.model.LineEntry
import com.jyhong.playground.core.model.TreemapEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.random.Random

@Singleton
class FakeChartDataSource @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val random = Random(System.currentTimeMillis())

    private data class ChartConfig(
        var count: Int = 10,
        var isPlaying: Boolean = true
    )

    private val configs = mapOf(
        ChartType.BAR to ChartConfig(count = 10),
        ChartType.LINE to ChartConfig(count = 20),
        ChartType.TREEMAP to ChartConfig(count = 8)
    )

    private val stockNames = listOf(
        "삼성전자", "LG에너지", "SK하이닉스", "삼성바이오", "LG화학",
        "현대차", "NAVER", "카카오", "기아", "POSCO홀딩스",
        "KB금융", "신한지주", "셀트리온", "삼성물산", "현대모비스",
        "LG전자", "하나금융", "SK이노", "SK텔레콤", "삼성생명"
    )

    private val _barData = MutableStateFlow<List<BarEntry>>(emptyList())
    private val _lineData = MutableStateFlow<List<LineEntry>>(emptyList())
    private val _treemapData = MutableStateFlow<List<TreemapEntry>>(emptyList())

    init {
        startGeneratingJob(ChartType.BAR, _barData)
        startGeneratingJob(ChartType.LINE, _lineData)
        startGeneratingJob(ChartType.TREEMAP, _treemapData)
    }

    fun getChartDataFlow(type: ChartType): StateFlow<List<ChartEntry>> {
        @Suppress("UNCHECKED_CAST")
        return when (type) {
            ChartType.BAR -> _barData as StateFlow<List<ChartEntry>>
            ChartType.LINE -> _lineData as StateFlow<List<ChartEntry>>
            ChartType.TREEMAP -> _treemapData as StateFlow<List<ChartEntry>>
        }
    }

    fun updateCount(type: ChartType, count: Int) {
        configs[type]?.count = count
        forceGenerate(type)
    }

    fun setPlayState(type: ChartType, isPlaying: Boolean) {
        configs[type]?.isPlaying = isPlaying
    }

    private fun <T : ChartEntry> startGeneratingJob(
        type: ChartType,
        flow: MutableStateFlow<List<T>>
    ) {
        scope.launch {
            while (isActive) {
                val config = configs[type] ?: return@launch

                // 재생 중일 때만 데이터 갱신
                if (config.isPlaying) {
                    val newData = generateData(type, config.count)
                    @Suppress("UNCHECKED_CAST")
                    flow.value = newData as List<T>
                }

                delay(1000L)
            }
        }
    }

    private fun forceGenerate(type: ChartType) {
        val config = configs[type] ?: return
        val newData = generateData(type, config.count)

        when (type) {
            ChartType.BAR -> _barData.value = newData as List<BarEntry>
            ChartType.LINE -> _lineData.value = newData as List<LineEntry>
            ChartType.TREEMAP -> _treemapData.value = newData as List<TreemapEntry>
        }
    }

    private fun generateData(type: ChartType, count: Int): List<ChartEntry> {
        return when (type) {
            ChartType.BAR -> List(count) {
                BarEntry(label = "B${it+1}", value = random.nextFloat() * 100f)
            }
            ChartType.LINE -> {
                val time = System.currentTimeMillis()
                List(count) {
                    LineEntry(label = "$it", value = random.nextFloat() * 100f, timestamp = time + it)
                }
            }
            ChartType.TREEMAP -> {
                List(count) { index ->
                    val name = stockNames.getOrElse(index) { "코스닥 ${index - stockNames.size + 1}호" }
                    val rankDecay = (index + 1).toFloat().pow(0.8f)
                    val baseSize = (1000f / rankDecay).coerceAtLeast(40f) // 최소 크기 40 보장

                    val randomWobble = baseSize * (random.nextFloat() * 0.3f - 0.5f)
                    val finalValue = baseSize + randomWobble
                    val change = (random.nextFloat() * 10f) - 5f

                    TreemapEntry(
                        id = UUID.randomUUID().toString(),
                        label = name,
                        value = finalValue,
                        changeRate = change
                    )
                }
            }

        }
    }
}

