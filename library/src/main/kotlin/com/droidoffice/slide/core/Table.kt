package com.droidoffice.slide.core

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.slide.format.LineStyle

/**
 * A table shape on a slide.
 * Maps to `p:graphicFrame` containing `a:tbl` in OOXML.
 */
class Table(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
    val rows: Int,
    val cols: Int,
) : Shape() {

    private val cells = Array(rows) { Array(cols) { TableCell() } }

    operator fun get(row: Int, col: Int): TableCell = cells[row][col]

    fun setCell(row: Int, col: Int, text: String) {
        cells[row][col].text = text
    }

    fun allCells(): Sequence<Triple<Int, Int, TableCell>> = sequence {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                yield(Triple(r, c, cells[r][c]))
            }
        }
    }
}

class TableCell {
    val paragraphs = mutableListOf<Paragraph>()
    var fill: Fill = Fill.None

    var text: String
        get() = paragraphs.joinToString("\n") { it.text }
        set(value) {
            paragraphs.clear()
            val para = Paragraph()
            para.addRun(value)
            paragraphs.add(para)
        }
}
