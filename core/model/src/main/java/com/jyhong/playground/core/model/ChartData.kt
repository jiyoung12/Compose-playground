package com.jyhong.playground.core.model

import java.util.UUID

enum class ChartType {
    BAR, LINE, TREEMAP
}

sealed interface ChartEntry {
    val id: String
    val label: String
    val value: Float
}

data class BarEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String,
    override val value: Float,
    val colorHex: String? = null
) : ChartEntry

data class LineEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String,
    override val value: Float,
    val timestamp: Long
) : ChartEntry

data class TreemapEntry(
    override val id: String = UUID.randomUUID().toString(),
    override val label: String,
    override val value: Float,
    val parentId: String? = null,
    val colorHex: String? = null,
    val changeRate : Float? = null
) : ChartEntry

data class ChartData(
    val title: String,
    val type: ChartType,
    val entries: List<ChartEntry>
)

data class TreemapGroup(
    val groupName: String,
    val children: List<TreemapEntry>
) {
    // 그룹의 크기는 자식들 값의 총합
    val totalValue: Float get() = children.sumOf { it.value.toDouble() }.toFloat()
}