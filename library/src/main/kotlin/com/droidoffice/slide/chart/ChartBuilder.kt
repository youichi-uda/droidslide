package com.droidoffice.slide.chart

/**
 * DSL builder for charts.
 *
 * ```kotlin
 * val chart = chartBuilder {
 *     type = ChartType.BAR
 *     title = "Sales"
 *     series("Q1", listOf(10.0, 20.0, 30.0))
 *     series("Q2", listOf(15.0, 25.0, 35.0))
 * }
 * ```
 */
class ChartBuilder {
    var type: ChartType = ChartType.BAR
    var title: String? = null
    var x: Long = 914400
    var y: Long = 914400
    var width: Long = 7315200
    var height: Long = 4572000
    private val _series = mutableListOf<ChartSeries>()

    fun series(name: String, values: List<Double>, categories: List<String> = emptyList()) {
        _series.add(ChartSeries(name, values, categories))
    }

    internal fun build(id: Int): SlideChart {
        val chart = SlideChart(id, "Chart $id", x, y, width, height, type = type, title = title)
        chart.series.addAll(_series)
        return chart
    }
}

fun chartBuilder(block: ChartBuilder.() -> Unit): ChartBuilder {
    val builder = ChartBuilder()
    builder.block()
    return builder
}
