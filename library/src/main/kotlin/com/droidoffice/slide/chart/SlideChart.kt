package com.droidoffice.slide.chart

import com.droidoffice.slide.core.Shape

/**
 * An embedded chart on a slide.
 */
class SlideChart(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
    val type: ChartType = ChartType.BAR,
    val title: String? = null,
) : Shape() {
    val series = mutableListOf<ChartSeries>()
    internal var rId: String = ""
}

data class ChartSeries(
    val name: String,
    val values: List<Double>,
    val categories: List<String> = emptyList(),
)

enum class ChartType(val ooxmlTag: String) {
    BAR("c:barChart"),
    LINE("c:lineChart"),
    PIE("c:pieChart"),
    AREA("c:areaChart"),
    SCATTER("c:scatterChart"),
}
