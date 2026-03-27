package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.chart.SlideChart
import com.droidoffice.slide.core.GroupShape
import com.droidoffice.slide.core.Paragraph
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.PresetShape
import com.droidoffice.slide.core.Shape
import com.droidoffice.slide.core.Table
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.core.TextRun
import com.droidoffice.slide.format.BulletType
import com.droidoffice.slide.format.LineDash
import com.droidoffice.slide.format.ShapeStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import com.droidoffice.slide.format.UnderlineStyle

/**
 * Builds slide XML content for individual shapes.
 */
object SlideWriter {

    /** Relationship counter per slide (for pictures). */
    internal var nextRelId = 2  // rId1 = slideLayout

    fun writeShapes(shapes: List<Shape>): String {
        nextRelId = 2
        return buildString {
            for (shape in shapes) {
                when (shape) {
                    is TextBox -> writeTextBox(shape)
                    is PresetShape -> writePresetShape(shape)
                    is Picture -> writePicture(shape)
                    is Table -> writeTable(shape)
                    is GroupShape -> writeGroupShape(shape)
                    is SlideChart -> {} // Charts written as separate parts by PptxWriter
                }
            }
        }
    }

    // -- TextBox --

    private fun StringBuilder.writeTextBox(tb: TextBox) {
        appendLine("""      <p:sp>""")
        appendLine("""        <p:nvSpPr>""")
        appendLine("""          <p:cNvPr id="${tb.id}" name="${esc(tb.name)}"/>""")
        appendLine("""          <p:cNvSpPr txBox="1"/>""")
        appendLine("""          <p:nvPr/>""")
        appendLine("""        </p:nvSpPr>""")
        appendLine("""        <p:spPr>""")
        writeTransform(tb)
        appendLine("""          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>""")
        appendLine("""          <a:noFill/>""")
        appendLine("""        </p:spPr>""")
        writeTxBody(tb.paragraphs)
        appendLine("""      </p:sp>""")
    }

    // -- PresetShape --

    private fun StringBuilder.writePresetShape(shape: PresetShape) {
        appendLine("""      <p:sp>""")
        appendLine("""        <p:nvSpPr>""")
        appendLine("""          <p:cNvPr id="${shape.id}" name="${esc(shape.name)}"/>""")
        appendLine("""          <p:cNvSpPr/>""")
        appendLine("""          <p:nvPr/>""")
        appendLine("""        </p:nvSpPr>""")
        appendLine("""        <p:spPr>""")
        writeTransform(shape)
        appendLine("""          <a:prstGeom prst="${shape.presetType.ooxmlValue}"><a:avLst/></a:prstGeom>""")
        writeShapeStyle(shape.shapeStyle)
        appendLine("""        </p:spPr>""")
        if (shape.paragraphs.isNotEmpty()) {
            writeTxBody(shape.paragraphs)
        }
        appendLine("""      </p:sp>""")
    }

    // -- Picture --

    private fun StringBuilder.writePicture(pic: Picture) {
        val rId = pic.rId.ifEmpty { "rId${nextRelId++}" }
        appendLine("""      <p:pic>""")
        appendLine("""        <p:nvPicPr>""")
        appendLine("""          <p:cNvPr id="${pic.id}" name="${esc(pic.name)}"/>""")
        appendLine("""          <p:cNvPicPr><a:picLocks noChangeAspect="1"/></p:cNvPicPr>""")
        appendLine("""          <p:nvPr/>""")
        appendLine("""        </p:nvPicPr>""")
        appendLine("""        <p:blipFill>""")
        appendLine("""          <a:blip r:embed="$rId"/>""")
        appendLine("""          <a:stretch><a:fillRect/></a:stretch>""")
        appendLine("""        </p:blipFill>""")
        appendLine("""        <p:spPr>""")
        writeTransform(pic)
        appendLine("""          <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>""")
        appendLine("""        </p:spPr>""")
        appendLine("""      </p:pic>""")
    }

    // -- Table --

    private fun StringBuilder.writeTable(table: Table) {
        appendLine("""      <p:graphicFrame>""")
        appendLine("""        <p:nvGraphicFramePr>""")
        appendLine("""          <p:cNvPr id="${table.id}" name="${esc(table.name)}"/>""")
        appendLine("""          <p:cNvGraphicFramePr><a:graphicFrameLocks noGrp="1"/></p:cNvGraphicFramePr>""")
        appendLine("""          <p:nvPr/>""")
        appendLine("""        </p:nvGraphicFramePr>""")
        appendLine("""        <p:xfrm>""")
        appendLine("""          <a:off x="${table.x}" y="${table.y}"/>""")
        appendLine("""          <a:ext cx="${table.width}" cy="${table.height}"/>""")
        appendLine("""        </p:xfrm>""")
        appendLine("""        <a:graphic>""")
        appendLine("""          <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/table">""")
        appendLine("""            <a:tbl>""")
        appendLine("""              <a:tblPr firstRow="1" bandRow="1"/>""")
        // Column widths (evenly distributed)
        appendLine("""              <a:tblGrid>""")
        val colWidth = table.width / table.cols
        for (c in 0 until table.cols) {
            appendLine("""                <a:gridCol w="$colWidth"/>""")
        }
        appendLine("""              </a:tblGrid>""")
        // Rows
        val rowHeight = table.height / table.rows
        for (r in 0 until table.rows) {
            appendLine("""              <a:tr h="$rowHeight">""")
            for (c in 0 until table.cols) {
                val cell = table[r, c]
                appendLine("""                <a:tc>""")
                // Cell text
                if (cell.paragraphs.isEmpty()) {
                    appendLine("""                  <a:txBody><a:bodyPr/><a:lstStyle/><a:p><a:endParaRPr/></a:p></a:txBody>""")
                } else {
                    append("                  ")
                    writeTxBody(cell.paragraphs)
                }
                appendLine("""                  <a:tcPr/>""")
                appendLine("""                </a:tc>""")
            }
            appendLine("""              </a:tr>""")
        }
        appendLine("""            </a:tbl>""")
        appendLine("""          </a:graphicData>""")
        appendLine("""        </a:graphic>""")
        appendLine("""      </p:graphicFrame>""")
    }

    // -- GroupShape --

    private fun StringBuilder.writeGroupShape(group: GroupShape) {
        appendLine("""      <p:grpSp>""")
        appendLine("""        <p:nvGrpSpPr>""")
        appendLine("""          <p:cNvPr id="${group.id}" name="${esc(group.name)}"/>""")
        appendLine("""          <p:cNvGrpSpPr/>""")
        appendLine("""          <p:nvPr/>""")
        appendLine("""        </p:nvGrpSpPr>""")
        appendLine("""        <p:grpSpPr>""")
        appendLine("""          <a:xfrm>""")
        appendLine("""            <a:off x="${group.x}" y="${group.y}"/>""")
        appendLine("""            <a:ext cx="${group.width}" cy="${group.height}"/>""")
        appendLine("""            <a:chOff x="${group.x}" y="${group.y}"/>""")
        appendLine("""            <a:chExt cx="${group.width}" cy="${group.height}"/>""")
        appendLine("""          </a:xfrm>""")
        appendLine("""        </p:grpSpPr>""")
        // Child shapes (reuse writeShapes logic)
        for (child in group.children) {
            when (child) {
                is TextBox -> writeTextBox(child)
                is PresetShape -> writePresetShape(child)
                is Picture -> writePicture(child)
                is Table -> writeTable(child)
                is GroupShape -> writeGroupShape(child)
            }
        }
        appendLine("""      </p:grpSp>""")
    }

    // -- Common helpers --

    private fun StringBuilder.writeTransform(shape: Shape) {
        val rotAttr = if (shape.rotation != 0.0) {
            """ rot="${(shape.rotation * 60000).toLong()}""""
        } else ""
        appendLine("""          <a:xfrm$rotAttr>""")
        appendLine("""            <a:off x="${shape.x}" y="${shape.y}"/>""")
        appendLine("""            <a:ext cx="${shape.width}" cy="${shape.height}"/>""")
        appendLine("""          </a:xfrm>""")
    }

    private fun StringBuilder.writeShapeStyle(style: ShapeStyle) {
        // Fill
        when (val fill = style.fill) {
            is Fill.Solid -> {
                val color = fill.color
                if (color is OfficeColor.Rgb) {
                    appendLine("""          <a:solidFill><a:srgbClr val="${color.toHex()}"/></a:solidFill>""")
                }
            }
            is Fill.None -> appendLine("""          <a:noFill/>""")
            else -> {}
        }
        // Line
        val line = style.line
        if (line.width > 0 || line.color != null) {
            append("""          <a:ln""")
            if (line.width > 0) append(""" w="${line.width}"""")
            appendLine(">")
            val lineColor = line.color
            if (lineColor is OfficeColor.Rgb) {
                appendLine("""            <a:solidFill><a:srgbClr val="${lineColor.toHex()}"/></a:solidFill>""")
            }
            if (line.dashStyle != LineDash.SOLID) {
                appendLine("""            <a:prstDash val="${line.dashStyle.ooxmlValue}"/>""")
            }
            appendLine("""          </a:ln>""")
        }
    }

    private fun StringBuilder.writeTxBody(paragraphs: List<Paragraph>) {
        appendLine("""        <p:txBody>""")
        appendLine("""          <a:bodyPr wrap="square" rtlCol="0"/>""")
        appendLine("""          <a:lstStyle/>""")
        if (paragraphs.isEmpty()) {
            appendLine("""          <a:p><a:endParaRPr/></a:p>""")
        } else {
            for (para in paragraphs) {
                writeParagraph(para)
            }
        }
        appendLine("""        </p:txBody>""")
    }

    private fun StringBuilder.writeParagraph(para: Paragraph) {
        append("          <a:p>")
        val style = para.style
        val hasProps = style.alignment != TextAlignment.LEFT ||
            style.level > 0 ||
            style.bulletType != BulletType.NONE
        if (hasProps) {
            append("""<a:pPr algn="${style.alignment.ooxmlValue}"""")
            if (style.level > 0) append(""" lvl="${style.level}"""")
            append(">")
            when (style.bulletType) {
                BulletType.CHAR -> append("""<a:buChar char="${esc(style.bulletChar ?: "\u2022")}"/>""")
                BulletType.AUTO_NUMBERED -> append("""<a:buAutoNum type="arabicPeriod"/>""")
                BulletType.NONE -> if (style.level > 0) append("<a:buNone/>")
            }
            append("</a:pPr>")
        }
        if (para.runs.isEmpty()) {
            append("<a:endParaRPr/>")
        } else {
            for (run in para.runs) {
                writeRun(run)
            }
        }
        appendLine("</a:p>")
    }

    private fun StringBuilder.writeRun(run: TextRun) {
        append("<a:r>")
        writeRunProps(run.style)
        append("<a:t>${esc(run.text)}</a:t>")
        append("</a:r>")
    }

    private fun StringBuilder.writeRunProps(style: TextStyle) {
        val hasProps = style.fontSize != null || style.bold || style.italic ||
            style.underline != UnderlineStyle.NONE || style.strikethrough ||
            style.fontName != null || style.color != null

        if (!hasProps) {
            append("<a:rPr lang=\"en-US\" dirty=\"0\"/>")
            return
        }

        append("<a:rPr lang=\"en-US\" dirty=\"0\"")
        style.fontSize?.let { append(""" sz="${(it * 100).toInt()}"""") }
        if (style.bold) append(""" b="1"""")
        if (style.italic) append(""" i="1"""")
        if (style.underline != UnderlineStyle.NONE) append(""" u="${style.underline.ooxmlValue}"""")
        if (style.strikethrough) append(""" strike="sngStrike"""")
        append(">")

        val color = style.color
        if (color is OfficeColor.Rgb) {
            append("""<a:solidFill><a:srgbClr val="${color.toHex()}"/></a:solidFill>""")
        }
        style.fontName?.let { append("""<a:latin typeface="${esc(it)}"/>""") }
        append("</a:rPr>")
    }

    private fun esc(text: String) = PptxWriter.escapeXml(text)
}
