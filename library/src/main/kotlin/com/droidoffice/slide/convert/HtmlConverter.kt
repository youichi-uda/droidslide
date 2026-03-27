package com.droidoffice.slide.convert

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.core.PresetShape
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.Slide
import com.droidoffice.slide.core.Table
import com.droidoffice.slide.core.TextBox

/**
 * Converts a presentation to HTML for preview/export.
 */
object HtmlConverter {

    fun convert(presentation: Presentation, title: String = "Presentation"): String = buildString {
        appendLine("<!DOCTYPE html>")
        appendLine("<html><head>")
        appendLine("<meta charset=\"UTF-8\">")
        appendLine("<title>${escapeHtml(title)}</title>")
        appendLine("<style>")
        appendLine("body { font-family: Calibri, sans-serif; background: #f0f0f0; margin: 20px; }")
        appendLine(".slide { background: white; margin: 20px auto; padding: 40px; box-shadow: 0 2px 8px rgba(0,0,0,0.15); position: relative; max-width: 960px; min-height: 540px; }")
        appendLine(".slide-num { position: absolute; bottom: 10px; right: 15px; color: #999; font-size: 12px; }")
        appendLine(".textbox { margin-bottom: 12px; }")
        appendLine(".notes { background: #fffde7; padding: 10px; margin: 5px auto; max-width: 960px; font-size: 13px; color: #555; }")
        appendLine("table { border-collapse: collapse; margin: 10px 0; }")
        appendLine("td, th { border: 1px solid #ccc; padding: 6px 10px; }")
        appendLine("</style>")
        appendLine("</head><body>")

        for ((index, slide) in presentation.slides.withIndex()) {
            appendLine(convertSlide(slide, index))
        }

        appendLine("</body></html>")
    }

    fun convertSlide(slide: Slide, slideIndex: Int): String = buildString {
        appendLine("""<div class="slide">""")
        appendLine("""<div class="slide-num">Slide ${slideIndex + 1}</div>""")

        for (shape in slide.shapes) {
            when (shape) {
                is TextBox -> {
                    appendLine("""<div class="textbox">""")
                    for (para in shape.paragraphs) {
                        append("<p style=\"margin:4px 0;")
                        append("text-align:${para.style.alignment.name.lowercase()};")
                        append("\">")
                        for (run in para.runs) {
                            val style = run.style
                            append("<span style=\"")
                            if (style.bold) append("font-weight:bold;")
                            if (style.italic) append("font-style:italic;")
                            style.fontSize?.let { append("font-size:${it}pt;") }
                            style.fontName?.let { append("font-family:'${escapeHtml(it)}';") }
                            val color = style.color
                            if (color is OfficeColor.Rgb) append("color:#${color.toHex()};")
                            append("\">")
                            append(escapeHtml(run.text))
                            append("</span>")
                        }
                        appendLine("</p>")
                    }
                    appendLine("</div>")
                }
                is PresetShape -> {
                    appendLine("""<div class="textbox">[${shape.presetType.name}] ${escapeHtml(shape.text)}</div>""")
                }
                is Table -> {
                    appendLine("<table>")
                    for (r in 0 until shape.rows) {
                        appendLine("<tr>")
                        for (c in 0 until shape.cols) {
                            appendLine("<td>${escapeHtml(shape[r, c].text)}</td>")
                        }
                        appendLine("</tr>")
                    }
                    appendLine("</table>")
                }
                else -> {}
            }
        }

        appendLine("</div>")

        // Notes
        val notes = slide.notes
        if (notes != null && notes.text.isNotBlank()) {
            appendLine("""<div class="notes">Notes: ${escapeHtml(notes.text)}</div>""")
        }
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
}
