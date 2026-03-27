package com.droidoffice.slide.core

import com.droidoffice.slide.chart.ChartBuilder
import com.droidoffice.slide.chart.SlideChart

/**
 * Represents a single slide in a presentation.
 */
class Slide internal constructor(var name: String = "") {

    private val _shapes = mutableListOf<Shape>()

    /** All shapes on this slide. */
    val shapes: List<Shape> get() = _shapes

    /** Whether this slide is hidden during slideshow. */
    var isHidden: Boolean = false

    /** Speaker notes for this slide. */
    var notes: SpeakerNotes? = null

    /** Relationship ID pointing to the slide layout. */
    internal var layoutRId: String = ""

    /** Internal relationship ID for this slide within the presentation. */
    internal var rId: String = ""

    /** Internal slide ID (unique within presentation). */
    internal var slideId: Int = 0

    // -- Shape operations --

    internal fun addShape(shape: Shape) {
        _shapes.add(shape)
    }

    fun addTextBox(
        x: Long, y: Long, width: Long, height: Long,
        text: String = "",
    ): TextBox {
        val id = nextShapeId++
        val tb = TextBox(id, "TextBox $id", x, y, width, height)
        if (text.isNotEmpty()) tb.setText(text)
        _shapes.add(tb)
        return tb
    }

    fun addPresetShape(
        x: Long, y: Long, width: Long, height: Long,
        type: PresetShapeType,
    ): PresetShape {
        val id = nextShapeId++
        val shape = PresetShape(id, "Shape $id", x, y, width, height, presetType = type)
        _shapes.add(shape)
        return shape
    }

    fun addPicture(
        x: Long, y: Long, width: Long, height: Long,
        imageData: ByteArray, format: ImageFormat,
    ): Picture {
        val id = nextShapeId++
        val pic = Picture(id, "Picture $id", x, y, width, height, imageData = imageData, format = format)
        _shapes.add(pic)
        return pic
    }

    fun addTable(
        x: Long, y: Long, width: Long, height: Long,
        rows: Int, cols: Int,
    ): Table {
        val id = nextShapeId++
        val table = Table(id, "Table $id", x, y, width, height, rows = rows, cols = cols)
        _shapes.add(table)
        return table
    }

    fun addChart(block: ChartBuilder.() -> Unit): SlideChart {
        val builder = ChartBuilder()
        builder.block()
        val chart = builder.build(nextShapeId++)
        _shapes.add(chart)
        return chart
    }

    fun removeShape(index: Int) {
        _shapes.removeAt(index)
    }

    fun removeShape(shape: Shape) {
        _shapes.remove(shape)
    }

    internal var nextShapeId: Int = 2  // 1 is reserved for slide background
}
