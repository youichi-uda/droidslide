package com.droidoffice.slide.core

import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.TextStyle

/**
 * A text box shape containing paragraphs of styled text.
 * Maps to `p:sp` with `p:txBody` in OOXML.
 */
class TextBox(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
) : Shape() {

    val paragraphs = mutableListOf<Paragraph>()

    fun addParagraph(text: String = "", style: ParagraphStyle = ParagraphStyle()): Paragraph {
        val para = Paragraph(style = style)
        if (text.isNotEmpty()) {
            para.addRun(text)
        }
        paragraphs.add(para)
        return para
    }

    /** Convenience: set single-paragraph text. */
    fun setText(text: String) {
        paragraphs.clear()
        addParagraph(text)
    }

    /** Concatenated text of all paragraphs. */
    val text: String
        get() = paragraphs.joinToString("\n") { it.text }
}

/**
 * A paragraph containing one or more styled text runs.
 * Maps to DrawingML `a:p`.
 */
data class Paragraph(
    val runs: MutableList<TextRun> = mutableListOf(),
    var style: ParagraphStyle = ParagraphStyle(),
) {
    fun addRun(text: String, style: TextStyle = TextStyle()): TextRun {
        val run = TextRun(text, style)
        runs.add(run)
        return run
    }

    val text: String get() = runs.joinToString("") { it.text }
}

/**
 * A run of text with uniform formatting.
 * Maps to DrawingML `a:r`.
 */
data class TextRun(
    var text: String,
    var style: TextStyle = TextStyle(),
)
