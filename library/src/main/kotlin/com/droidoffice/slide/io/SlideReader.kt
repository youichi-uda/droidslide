package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.core.ooxml.SaxReader
import com.droidoffice.slide.core.Paragraph
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.PresetShape
import com.droidoffice.slide.core.PresetShapeType
import com.droidoffice.slide.core.Slide
import com.droidoffice.slide.core.Table
import com.droidoffice.slide.core.TableCell
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.core.TextRun
import com.droidoffice.slide.format.BulletType
import com.droidoffice.slide.format.LineDash
import com.droidoffice.slide.format.LineStyle
import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.ShapeStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import com.droidoffice.slide.format.UnderlineStyle
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream

/**
 * SAX-based reader for individual slide XML.
 */
object SlideReader {

    /** Image data loaded from the OOXML package, keyed by rId. */
    internal var mediaLoader: ((rId: String) -> Pair<ByteArray, String>?)? = null

    fun read(input: InputStream, slide: Slide) {
        SaxReader.parse(input, SlideHandler(slide))
    }

    private class SlideHandler(private val slide: Slide) : DefaultHandler() {

        private val textBuffer = StringBuilder()

        // Element path tracking
        private var inSpTree = false
        private var inSp = false
        private var inPic = false
        private var inGraphicFrame = false
        private var inTbl = false
        private var inTr = false
        private var inTc = false
        private var inTxBody = false
        private var inParagraph = false
        private var inRun = false
        private var inRunProps = false
        private var inParaProps = false
        private var inText = false
        private var inSolidFill = false
        private var inSpPr = false
        private var inXfrm = false
        private var inLn = false

        // Current shape data
        private var shapeId: Int = 0
        private var shapeName: String = ""
        private var shapeX: Long = 0
        private var shapeY: Long = 0
        private var shapeWidth: Long = 0
        private var shapeHeight: Long = 0
        private var shapeRotation: Double = 0.0
        private var hasTxBody = false
        private var isTxBox = false
        private var presetGeom: String? = null

        // Shape style
        private var fillColor: OfficeColor? = null
        private var lineWidth: Long = 0
        private var lineColor: OfficeColor? = null
        private var lineDash: LineDash = LineDash.SOLID
        private var shapeFill: Fill = Fill.None

        // Picture
        private var picBlipRId: String = ""

        // Table
        private var tableRows = 0
        private var tableCols = 0
        private var currentRow = 0
        private var currentCol = 0
        private val tableCellTexts = mutableListOf<MutableList<String>>()

        // Paragraph/run data
        private val paragraphs = mutableListOf<Paragraph>()
        private var currentParaStyle = ParagraphStyle()
        private val currentRuns = mutableListOf<TextRun>()
        private var currentRunStyle = TextStyle()
        private var pendingColor: OfficeColor? = null

        private fun resetShapeState() {
            shapeId = 0; shapeName = ""
            shapeX = 0; shapeY = 0; shapeWidth = 0; shapeHeight = 0
            shapeRotation = 0.0
            hasTxBody = false; isTxBox = false
            presetGeom = null
            fillColor = null; lineWidth = 0; lineColor = null; lineDash = LineDash.SOLID
            shapeFill = Fill.None
            picBlipRId = ""
            paragraphs.clear()
        }

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            textBuffer.clear()
            when (localName) {
                "spTree" -> inSpTree = true

                // -- p:sp (TextBox or PresetShape) --
                "sp" -> if (inSpTree && !inGraphicFrame) {
                    inSp = true
                    resetShapeState()
                }
                "cNvSpPr" -> if (inSp) {
                    isTxBox = attributes.getValue("txBox") == "1"
                }

                // -- p:pic --
                "pic" -> if (inSpTree) {
                    inPic = true
                    resetShapeState()
                }

                // -- p:graphicFrame (Table) --
                "graphicFrame" -> if (inSpTree) {
                    inGraphicFrame = true
                    resetShapeState()
                }

                // -- Common element parsing --
                "cNvPr" -> if ((inSp || inPic || inGraphicFrame) && !inTxBody) {
                    shapeId = attributes.getValue("id")?.toIntOrNull() ?: 0
                    shapeName = attributes.getValue("name") ?: ""
                }
                "spPr" -> if (inSp || inPic) inSpPr = true
                "xfrm" -> if (inSpPr || inGraphicFrame) {
                    inXfrm = true
                    val rot = attributes.getValue("rot")?.toLongOrNull()
                    if (rot != null) shapeRotation = rot / 60000.0
                }
                "off" -> if (inXfrm) {
                    shapeX = attributes.getValue("x")?.toLongOrNull() ?: shapeX
                    shapeY = attributes.getValue("y")?.toLongOrNull() ?: shapeY
                }
                "ext" -> if (inXfrm) {
                    shapeWidth = attributes.getValue("cx")?.toLongOrNull() ?: shapeWidth
                    shapeHeight = attributes.getValue("cy")?.toLongOrNull() ?: shapeHeight
                }
                "prstGeom" -> if (inSpPr) {
                    presetGeom = attributes.getValue("prst")
                }

                // Shape fill (inside spPr)
                "solidFill" -> {
                    if (inSpPr && !inRunProps && !inLn) {
                        inSolidFill = true
                    } else if (inLn) {
                        inSolidFill = true
                    } else if (inRunProps) {
                        inSolidFill = true
                    }
                }
                "srgbClr" -> if (inSolidFill) {
                    val hex = attributes.getValue("val")
                    if (hex != null) {
                        val color = OfficeColor.Rgb.fromHex(hex)
                        when {
                            inRunProps -> pendingColor = color
                            inLn -> lineColor = color
                            inSpPr -> fillColor = color
                        }
                    }
                }
                "noFill" -> if (inSpPr && !inLn) shapeFill = Fill.None
                "ln" -> if (inSpPr) {
                    inLn = true
                    lineWidth = attributes.getValue("w")?.toLongOrNull() ?: 0
                }
                "prstDash" -> if (inLn) {
                    lineDash = LineDash.fromOoxml(attributes.getValue("val"))
                }

                // -- Picture blip --
                "blip" -> if (inPic) {
                    picBlipRId = attributes.getValue(
                        "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed"
                    ) ?: attributes.getValue("r:embed") ?: ""
                }

                // -- Table --
                "tbl" -> if (inGraphicFrame) {
                    inTbl = true
                    tableRows = 0; tableCols = 0
                    tableCellTexts.clear()
                }
                "gridCol" -> if (inTbl) tableCols++
                "tr" -> if (inTbl) {
                    inTr = true
                    currentRow = tableRows++
                    currentCol = 0
                    tableCellTexts.add(mutableListOf())
                }
                "tc" -> if (inTr) {
                    inTc = true
                    paragraphs.clear()
                }

                // -- Text body --
                "txBody" -> {
                    if (inSp || inTc) {
                        inTxBody = true
                        hasTxBody = true
                        paragraphs.clear()
                    }
                }
                "p" -> if (inTxBody) {
                    inParagraph = true
                    currentParaStyle = ParagraphStyle()
                    currentRuns.clear()
                }
                "pPr" -> if (inParagraph) {
                    inParaProps = true
                    currentParaStyle.alignment = TextAlignment.fromOoxml(attributes.getValue("algn"))
                    val lvl = attributes.getValue("lvl")?.toIntOrNull()
                    if (lvl != null) currentParaStyle.level = lvl
                }
                "buChar" -> if (inParaProps) {
                    currentParaStyle.bulletType = BulletType.CHAR
                    currentParaStyle.bulletChar = attributes.getValue("char")
                }
                "buAutoNum" -> if (inParaProps) {
                    currentParaStyle.bulletType = BulletType.AUTO_NUMBERED
                }
                "buNone" -> if (inParaProps) {
                    currentParaStyle.bulletType = BulletType.NONE
                }
                "r" -> if (inParagraph) {
                    inRun = true
                    currentRunStyle = TextStyle()
                    pendingColor = null
                }
                "rPr" -> if (inRun || inParagraph) {
                    inRunProps = true
                    if (inRun) {
                        val sz = attributes.getValue("sz")?.toIntOrNull()
                        if (sz != null) currentRunStyle.fontSize = sz / 100.0
                        currentRunStyle.bold = attributes.getValue("b") == "1"
                        currentRunStyle.italic = attributes.getValue("i") == "1"
                        currentRunStyle.underline = UnderlineStyle.fromOoxml(attributes.getValue("u"))
                        val strike = attributes.getValue("strike")
                        currentRunStyle.strikethrough = (strike == "sngStrike" || strike == "dblStrike")
                    }
                }
                "latin" -> if (inRunProps && inRun) {
                    currentRunStyle.fontName = attributes.getValue("typeface")
                }
                "t" -> if (inRun || inParagraph) inText = true
            }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            if (inText) textBuffer.append(ch, start, length)
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            when (localName) {
                "spTree" -> inSpTree = false

                "sp" -> if (inSp) {
                    if (hasTxBody && isTxBox) {
                        val tb = TextBox(shapeId, shapeName, shapeX, shapeY, shapeWidth, shapeHeight, shapeRotation)
                        tb.paragraphs.addAll(paragraphs)
                        slide.addShape(tb)
                    } else {
                        // PresetShape or generic shape
                        val type = PresetShapeType.fromOoxml(presetGeom)
                        if (type != null) {
                            val ps = PresetShape(shapeId, shapeName, shapeX, shapeY, shapeWidth, shapeHeight, shapeRotation, type)
                            ps.paragraphs.addAll(paragraphs)
                            val style = ShapeStyle()
                            if (fillColor != null) style.fill = Fill.Solid(fillColor!!)
                            if (lineWidth > 0 || lineColor != null) {
                                style.line = LineStyle(lineWidth, lineColor, lineDash)
                            }
                            ps.shapeStyle = style
                            slide.addShape(ps)
                        } else if (hasTxBody) {
                            // Fallback: treat as textbox
                            val tb = TextBox(shapeId, shapeName, shapeX, shapeY, shapeWidth, shapeHeight, shapeRotation)
                            tb.paragraphs.addAll(paragraphs)
                            slide.addShape(tb)
                        }
                    }
                    if (shapeId >= slide.nextShapeId) slide.nextShapeId = shapeId + 1
                    inSp = false
                }

                "pic" -> if (inPic) {
                    // Try to load image data
                    val mediaResult = mediaLoader?.invoke(picBlipRId)
                    if (mediaResult != null) {
                        val (data, ext) = mediaResult
                        val fmt = com.droidoffice.slide.core.ImageFormat.entries.find { it.extension == ext }
                            ?: com.droidoffice.slide.core.ImageFormat.PNG
                        val pic = Picture(shapeId, shapeName, shapeX, shapeY, shapeWidth, shapeHeight, shapeRotation, data, fmt)
                        pic.rId = picBlipRId
                        slide.addShape(pic)
                    }
                    if (shapeId >= slide.nextShapeId) slide.nextShapeId = shapeId + 1
                    inPic = false
                }

                "graphicFrame" -> if (inGraphicFrame) {
                    if (inTbl && tableRows > 0 && tableCols > 0) {
                        val table = Table(shapeId, shapeName, shapeX, shapeY, shapeWidth, shapeHeight, shapeRotation, tableRows, tableCols)
                        for (r in tableCellTexts.indices) {
                            for (c in tableCellTexts[r].indices) {
                                if (c < tableCols) table.setCell(r, c, tableCellTexts[r][c])
                            }
                        }
                        slide.addShape(table)
                    }
                    if (shapeId >= slide.nextShapeId) slide.nextShapeId = shapeId + 1
                    inGraphicFrame = false
                    inTbl = false
                }

                "tr" -> inTr = false
                "tc" -> if (inTc) {
                    val cellText = paragraphs.joinToString("\n") { it.text }
                    if (currentRow < tableCellTexts.size) {
                        tableCellTexts[currentRow].add(cellText)
                    }
                    currentCol++
                    inTc = false
                }

                "spPr" -> {
                    if (fillColor != null && shapeFill !is Fill.None) {
                        // already set
                    }
                    inSpPr = false
                }
                "xfrm" -> inXfrm = false
                "ln" -> inLn = false
                "txBody" -> inTxBody = false
                "p" -> if (inParagraph) {
                    paragraphs.add(Paragraph(runs = currentRuns.toMutableList(), style = currentParaStyle))
                    inParagraph = false
                }
                "pPr" -> inParaProps = false
                "r" -> inRun = false
                "rPr" -> inRunProps = false
                "solidFill" -> {
                    if (pendingColor != null && inRun) currentRunStyle.color = pendingColor
                    inSolidFill = false
                }
                "t" -> if (inText) {
                    if (inRun) currentRuns.add(TextRun(textBuffer.toString(), currentRunStyle.copy()))
                    inText = false
                }
            }
        }
    }
}
