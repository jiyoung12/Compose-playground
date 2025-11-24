package com.jyhong.playground.core.chart_engine.treemap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jyhong.playground.core.model.TreemapEntry
import androidx.core.graphics.toColorInt
import com.jyhong.playground.core.model.TreemapGroup
import java.util.UUID
import kotlin.math.abs


@OptIn(ExperimentalTextApi::class) // drawText 사용을 위해
@Composable
fun TreemapChart(
    entries: List<TreemapEntry>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val nodes = TreemapCalculator.computeLayout(entries, canvasWidth, canvasHeight)

        nodes.forEach { node ->
            val rect = node.rect
            val entry = node.entry

            val color = calculateStockColor(node.entry.changeRate ?: 0f, surfaceColor)
//            val color = entry.colorHex?.let { Color(it.toColorInt()) }
//                ?: generateStableColor(entry.label)

            // 사각형 채우기
            drawRect(
                color = color,
                topLeft = Offset(
                    rect.left,
                    rect.top
                ),
                size = Size(rect.width, rect.height)
            )

            // 테두리
            drawRect(
                color = Color.White,
                topLeft = Offset(
                    rect.left,
                    rect.top
                ),
                size = Size(rect.width, rect.height),
                style = Stroke(width = 4f)
            )

            // 텍스트
            if (rect.width > 50 && rect.height > 30) { // 넘 작으면 넘기기
                val textLayoutResult = textMeasurer.measure(
                    text = "${entry.label}\n${entry.value.toInt()}",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // 텍스트 중앙 정렬 계산
                val textOffsetX = rect.left + (rect.width - textLayoutResult.size.width) / 2
                val textOffsetY = rect.top + (rect.height - textLayoutResult.size.height) / 2

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(textOffsetX, textOffsetY)
                )
            }
        }
    }
}

@Composable
fun StockTreemapChart(
    groups: List<TreemapGroup>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainer

    Canvas(modifier = modifier.fillMaxSize().background(Color(0xFF202124))) { // 검은 배경
        val groupsLayout = TreemapCalculator.computeGroupedLayout(
            groups = groups,
            width = size.width,
            height = size.height,
            groupPadding = 10f, // 섹터 사이 간격
            headerHeight = 50f  // 헤더 높이
        )

        groupsLayout.forEach { (group, nodes) ->
            if (nodes.isNotEmpty()) {
                // 노드들의 전체 영역(Bounding Box)의 상단에 헤더가 있음
                val firstRect = nodes.first().rect
                // 대략적인 헤더 위치 추정 (정석은 계산기에서 헤더 Rect를 따로 줘야 함)
                val headerY = firstRect.top - 40f // TODO 이거 계산하는걸로 변경

                drawText(
                    textMeasurer = textMeasurer,
                    text = "${group.groupName} >",
                    topLeft = Offset(firstRect.left, headerY),
                    style = TextStyle(color = Color.LightGray, fontSize = 14.sp)
                )
            }

            nodes.forEach { node ->
                val rect = node.rect
                val entry = node.entry

                val color = calculateStockColor(
                    changeRate = entry.changeRate ?: 0f,
                    surfaceColor = surfaceColor
                )

                drawRect(
                    color = color,
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height)
                )

                drawRect(
                    color = Color(0xFF202124),
                    topLeft = Offset(rect.left, rect.top),
                    size = Size(rect.width, rect.height),
                    style = Stroke(width = 2f)
                )

                if (rect.width > 60 && rect.height > 40) {
                    val label = "${entry.label}\n${String.format("%.2f%%", entry.changeRate)}"
                    val textLayout = textMeasurer.measure(
                        text = label,
                        style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    )

                    drawText(
                        textLayoutResult = textLayout,
                        topLeft = Offset(
                            rect.left + (rect.width - textLayout.size.width) / 2,
                            rect.top + (rect.height - textLayout.size.height) / 2
                        )
                    )
                }
            }
        }
    }
}val BaseStockRed = Color(0xFFF44336) // Material Red 500
val BaseStockBlue = Color(0xFF2979FF) // Blue A400

fun calculateStockColor(changeRate: Float, surfaceColor: Color): Color {
    val intensity = (abs(changeRate) / 5f).coerceIn(0.1f, 1f) // 최소 10%는 섞이게
    val targetColor = if (changeRate >= 0) BaseStockRed else BaseStockBlue

    // 배경색과 주식색을 섞음
    return lerp(start = surfaceColor, stop = targetColor, fraction = intensity)
}
/**
 * 라벨 이름을 기반으로 항상 같은 색상을 반환
 */
fun generateStableColor(key: String): Color {
    val hash = key.hashCode()
    // 파스텔 톤 색상 생성
    val r = (hash and 0xFF0000 shr 16) % 156 + 100 // 100~255
    val g = (hash and 0x00FF00 shr 8) % 156 + 100
    val b = (hash and 0x0000FF) % 156 + 100
    return Color(r, g, b)
}


@Preview(
    name = "Treemap Light Mode",
    showBackground = true,
    widthDp = 400,
    heightDp = 400 // 정사각형 비율로 확인
)
@Composable
private fun TreemapChartPreview() {
    val mockData = listOf(
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "삼성전자",
            value = 1400f,       // 시가총액 (크기)
            changeRate = 3.47f  // 등락률 (색상: 연한 빨강)
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "LG에너지..",
            value = 350f,
            changeRate = 4.91f  // 급등 (진한 빨강)
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "SK하이닉스",
            value = 280f,
            changeRate = 0.11f  // 보합 (아주 연한 빨강)
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "삼성바이오..",
            value = 200f,
            changeRate = -0.63f // 하락 (연한 파랑)
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "LG화학",
            value = 180f,
            changeRate = 2.40f  // 상승 (빨강)
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "현대차",
            value = 150f,
            changeRate = 1.95f
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "NAVER",
            value = 140f,
            changeRate = 0.25f
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "카카오",
            value = 130f,
            changeRate = -0.48f
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "기아",
            value = 120f,
            changeRate = 2.11f
        ),
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "POSCO..",
            value = 110f,
            changeRate = 2.10f
        ),
        TreemapEntry(id = UUID.randomUUID().toString(), label = "KB금융", value = 80f, changeRate = 0.34f),
        TreemapEntry(id = UUID.randomUUID().toString(), label = "신한지주", value = 75f, changeRate = 0.79f),
        TreemapEntry(id = UUID.randomUUID().toString(), label = "셀트리온", value = 70f, changeRate = -0.43f),
        TreemapEntry(id = UUID.randomUUID().toString(), label = "삼성물산", value = 60f, changeRate = -0.08f),
        TreemapEntry(id = UUID.randomUUID().toString(), label = "현대모비스", value = 50f, changeRate = 1.39f)
    )

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            TreemapChart(
                entries = mockData,
                modifier = Modifier.padding(16.dp) // 약간의 여백
            )
        }
    }
}

@Preview(
    name = "Treemap Many Items",
    showBackground = true,
    widthDp = 360,
    heightDp = 640
)
@Composable
private fun TreemapChartManyItemsPreview() {
    val mockData = List(30) { index ->
        TreemapEntry(
            id = UUID.randomUUID().toString(),
            label = "Item $index",
            value = (10..200).random().toFloat()
        )
    }

    MaterialTheme {
        Surface {
            TreemapChart(
                entries = mockData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview(widthDp = 600, heightDp = 400)
@Composable
fun StockTreemapPreview() { // TODO 이거 다시 확인해보기
    val groups = listOf(
        TreemapGroup("전기/전자", listOf(
            TreemapEntry(label = "삼성전자", value = 4.5f), // +4.5% (진한 빨강)
            TreemapEntry(label = "SK하이닉스", value = 2.1f),
            TreemapEntry(label = "LG에너지", value = 5.0f), // 상한가 느낌
            TreemapEntry(label = "삼성SDI", value = -1.2f) // 하락 (연한 파랑)
        )),
        TreemapGroup("화학", listOf(
            TreemapEntry(label = "LG화학", value = 3.2f),
            TreemapEntry(label = "롯데케미칼", value = -2.5f),
            TreemapEntry(label = "한화솔루션", value = 1.0f)
        )),
        TreemapGroup("서비스업", listOf(
            TreemapEntry(label = "NAVER", value = -0.5f), // 보합
            TreemapEntry(label = "카카오", value = -3.8f), // 폭락 (진한 파랑)
            TreemapEntry(label = "크래프톤", value = 2.0f)
        ))
    )

    MaterialTheme {
        StockTreemapChart(groups = groups)
    }
}