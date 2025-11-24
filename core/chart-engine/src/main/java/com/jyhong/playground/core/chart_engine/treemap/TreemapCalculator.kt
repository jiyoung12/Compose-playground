package com.jyhong.playground.core.chart_engine.treemap

import androidx.compose.ui.geometry.Rect
import com.jyhong.playground.core.model.TreemapEntry
import com.jyhong.playground.core.model.TreemapGroup

data class TreemapNode(
    val rect: Rect,
    val entry: TreemapEntry
)

object TreemapCalculator {

    fun computeGroupedLayout(
        groups: List<TreemapGroup>,
        width: Float,
        height: Float,
        groupPadding: Float = 8f, // 그룹 간 간격
        headerHeight: Float = 40f // 헤더 공간
    ): List<Pair<TreemapGroup, List<TreemapNode>>> {

        val groupEntries = groups.map { group ->
            TreemapEntry(
                id = group.groupName,
                label = group.groupName,
                value = group.totalValue
            )
        }

        val groupNodes = computeLayout(groupEntries, width, height)

        return groupNodes.map { groupNode ->
            val originalGroup = groups.find { it.groupName == groupNode.entry.id }!!

            val contentRect = groupNode.rect.deflate(groupPadding / 2) // 안쪽 여백

            // 헤더 높이만큼 윗부분을 비워둠 (여기에 텍스트 그릴 예정)
            val stockAreaRect = Rect(
                left = contentRect.left,
                top = contentRect.top + headerHeight,
                right = contentRect.right,
                bottom = contentRect.bottom
            )

            if (stockAreaRect.width <= 0 || stockAreaRect.height <= 0) {
                originalGroup to emptyList()
            } else {
                val childNodes = computeLayout(
                    originalGroup.children,
                    stockAreaRect.width,
                    stockAreaRect.height
                )

                val offsetNodes = childNodes.map { child ->
                    child.copy(
                        rect = child.rect.translate(stockAreaRect.left, stockAreaRect.top)
                    )
                }

                originalGroup to offsetNodes
            }
        }
    }

    /**
     * 메인 계산 함수
     * @param entries: 데이터 리스트
     * @param width: 캔버스 가로 길이
     * @param height: 캔버스 세로 길이
     */
    fun computeLayout(entries: List<TreemapEntry>, width: Float, height: Float): List<TreemapNode> {
        if (entries.isEmpty()) return emptyList()

        val sortedEntries = entries.sortedByDescending { it.value } // 큰 값 먼저 정렬
        val totalRect = Rect(0f, 0f, width, height)
        return splitRect(totalRect, sortedEntries)
    }

    /**
     * 사각형을 분할함수
     */
    private fun splitRect(currentRect: Rect, entries: List<TreemapEntry>): List<TreemapNode> {
        if (entries.isEmpty()) return emptyList()

        if (entries.size == 1) {
            return listOf(TreemapNode(currentRect, entries.first()))
        }

        val totalValue = entries.sumOf { it.value.toDouble() }.toFloat()

        var midIndex = 0
        var currentSum = 0f
        val halfValue = totalValue / 2

        for (i in entries.indices) {
            currentSum += entries[i].value
            if (currentSum >= halfValue) {
                midIndex = i + 1 // 포함해서 자름
                break
            }
        }
        // 혹시 모르니 경계 처리
        if (midIndex >= entries.size) midIndex = entries.size - 1
        if (midIndex < 1) midIndex = 1

        val leftGroup = entries.subList(0, midIndex)
        val rightGroup = entries.subList(midIndex, entries.size)

        val leftSum = leftGroup.sumOf { it.value.toDouble() }.toFloat()
        totalValue - leftSum // 나머지

        val isHorizontalSplit = currentRect.width > currentRect.height

        val (leftRect, rightRect) = if (isHorizontalSplit) {
            // 가로로 기니까 -> 세로선으로 분할 (좌/우)
            val splitX = currentRect.left + (currentRect.width * (leftSum / totalValue))
            Pair(
                Rect(currentRect.left, currentRect.top, splitX, currentRect.bottom),
                Rect(splitX, currentRect.top, currentRect.right, currentRect.bottom)
            )
        } else {
            // 세로로 기니까 -> 가로선으로 분할 (상/하)
            val splitY = currentRect.top + (currentRect.height * (leftSum / totalValue))
            Pair(
                Rect(currentRect.left, currentRect.top, currentRect.right, splitY),
                Rect(currentRect.left, splitY, currentRect.right, currentRect.bottom)
            )
        }

        // 재귀 호출하여 합침
        return splitRect(leftRect, leftGroup) + splitRect(rightRect, rightGroup)
    }
}