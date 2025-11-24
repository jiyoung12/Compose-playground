package com.jyhong.playground.feature.treemap_chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jyhong.playground.core.domain.usecase.GetChartDataUseCase
import com.jyhong.playground.core.domain.usecase.ToggleChartPlayStateUseCase
import com.jyhong.playground.core.domain.usecase.UpdateChartCountUseCase
import com.jyhong.playground.core.model.ChartType
import com.jyhong.playground.core.model.TreemapEntry
import com.jyhong.playground.core.model.TreemapGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TreemapUiState(
    val groups: List<TreemapGroup> = emptyList(),
    val itemCount: Int = 10,
    val isPlaying: Boolean = true
)

@HiltViewModel
class TreemapViewModel @Inject constructor(
    getChartDataUseCase: GetChartDataUseCase,
    private val updateChartCountUseCase: UpdateChartCountUseCase,
    private val toggleChartPlayStateUseCase: ToggleChartPlayStateUseCase
) : ViewModel() {

    private val _controlState = MutableStateFlow(
        TreemapUiState(itemCount = 15, isPlaying = true)
    )

    private val _chartDataFlow = getChartDataUseCase(ChartType.TREEMAP)

    val uiState: StateFlow<TreemapUiState> = combine(
        _controlState,
        _chartDataFlow
    ) { control, chartData ->
        val treemapEntries = chartData.entries.filterIsInstance<TreemapEntry>()
        val group = TreemapGroup(
            groupName = "KOSPI Market",
            children = treemapEntries
        )

        control.copy(groups = listOf(group))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TreemapUiState()
    )


    fun onCountChanged(count: Int) {
        _controlState.value = _controlState.value.copy(itemCount = count)
        viewModelScope.launch {
            updateChartCountUseCase(ChartType.TREEMAP, count)
        }
    }

    fun onPlayPauseClicked() {
        val newPlayingState = !_controlState.value.isPlaying
        _controlState.value = _controlState.value.copy(isPlaying = newPlayingState)
        viewModelScope.launch {
            toggleChartPlayStateUseCase(ChartType.TREEMAP, newPlayingState)
        }
    }
}