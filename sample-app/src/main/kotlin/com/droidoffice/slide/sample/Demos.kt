package com.droidoffice.slide.sample

import android.content.Context
import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.chart.ChartType
import com.droidoffice.slide.convert.HtmlConverter
import com.droidoffice.slide.core.ImageFormat
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.PresetShape
import com.droidoffice.slide.core.PresetShapeType
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.SlideSize
import com.droidoffice.slide.core.SpeakerNotes
import com.droidoffice.slide.core.Table
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.format.BulletType
import com.droidoffice.slide.format.LineStyle
import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.ShapeStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import com.droidoffice.slide.format.UnderlineStyle
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

object Demos {

    // -------------------------------------------------------
    // 1. Basic Read/Write
    // -------------------------------------------------------
    fun basicReadWrite(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()

        // Multiple slides with various content
        val slide1 = pres.addSlide()
        slide1.addTextBox(914400, 2000000, 7315200, 1500000, "Hello DroidSlide!")

        val slide2 = pres.addSlide()
        slide2.addTextBox(914400, 914400, 7315200, 1500000, "Second slide")
        slide2.addTextBox(914400, 2743200, 7315200, 1500000, "日本語テキストもサポート")

        // Save to file
        val file = File(context.filesDir, "basic.pptx")
        file.outputStream().use { pres.save(it) }
        log.appendLine("Saved: ${file.length()} bytes")

        // Reload and verify
        val loaded = file.inputStream().use { Presentation.open(it) }
        log.appendLine("Loaded: ${loaded.slideCount} slides")
        check(loaded.slideCount == 2) { "Expected 2 slides" }

        for ((i, s) in loaded.slides.withIndex()) {
            val texts = s.shapes.filterIsInstance<TextBox>().map { it.text }
            log.appendLine("  Slide ${i + 1}: $texts")
        }

        val jpText = (loaded.slides[1].shapes[1] as TextBox).text
        check(jpText == "日本語テキストもサポート") { "Japanese round-trip failed" }
        log.appendLine("Japanese round-trip: OK")

        return log.toString()
    }

    // -------------------------------------------------------
    // 2. Styled Text
    // -------------------------------------------------------
    fun styledText(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()
        val slide = pres.addSlide()

        val tb = slide.addTextBox(914400, 914400, 7315200, 4500000)

        // Title with center alignment and large font
        tb.addParagraph("Presentation Title", ParagraphStyle(alignment = TextAlignment.CENTER))
            .runs[0].style = TextStyle(bold = true, fontSize = 36.0, color = OfficeColor.Rgb(68, 114, 196))

        // Mixed styles in one paragraph
        val mixed = tb.addParagraph()
        mixed.addRun("Bold", TextStyle(bold = true))
        mixed.addRun(", ", TextStyle())
        mixed.addRun("Italic", TextStyle(italic = true))
        mixed.addRun(", ", TextStyle())
        mixed.addRun("Underline", TextStyle(underline = UnderlineStyle.SINGLE))
        mixed.addRun(", ", TextStyle())
        mixed.addRun("Strikethrough", TextStyle(strikethrough = true))
        mixed.addRun(", ", TextStyle())
        mixed.addRun("Red Arial 18pt", TextStyle(
            color = OfficeColor.Rgb(255, 0, 0),
            fontName = "Arial",
            fontSize = 18.0,
        ))

        // Bullet list
        tb.addParagraph("First bullet", ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022"))
        tb.addParagraph("Second bullet", ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022"))
        tb.addParagraph("Numbered", ParagraphStyle(bulletType = BulletType.AUTO_NUMBERED))

        // Japanese and right-aligned
        tb.addParagraph("日本語テキスト：こんにちは世界", ParagraphStyle(alignment = TextAlignment.RIGHT))

        val file = File(context.filesDir, "styled.pptx")
        file.outputStream().use { pres.save(it) }

        // Reload and verify
        val loaded = file.inputStream().use { Presentation.open(it) }
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        log.appendLine("Paragraphs: ${loadedTb.paragraphs.size}")

        check(loadedTb.paragraphs[0].runs[0].style.bold) { "Bold not preserved" }
        check(loadedTb.paragraphs[0].runs[0].style.fontSize == 36.0) { "Font size not preserved" }
        log.appendLine("Bold + fontSize 36: OK")

        val mixedLoaded = loadedTb.paragraphs[1]
        check(mixedLoaded.runs[0].style.bold) { "Mixed bold not preserved" }
        check(mixedLoaded.runs[2].style.italic) { "Mixed italic not preserved" }
        log.appendLine("Mixed styles (bold, italic, underline, strike, color, font): OK")

        val jpPara = loadedTb.paragraphs.last()
        check(jpPara.text.contains("こんにちは世界")) { "Japanese not preserved" }
        check(jpPara.style.alignment == TextAlignment.RIGHT) { "Right alignment not preserved" }
        log.appendLine("Japanese + right alignment: OK")

        return log.toString()
    }

    // -------------------------------------------------------
    // 3. Shapes, Images & Tables
    // -------------------------------------------------------
    fun shapesAndImages(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()
        val slide = pres.addSlide()

        // Preset shapes with fill and line
        val rect = slide.addPresetShape(914400, 914400, 3000000, 2000000, PresetShapeType.ROUND_RECT)
        rect.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(68, 114, 196)),
            line = LineStyle(width = 12700, color = OfficeColor.Rgb(0, 0, 0)),
        )

        val ellipse = slide.addPresetShape(4500000, 914400, 2500000, 2000000, PresetShapeType.ELLIPSE)
        ellipse.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(237, 125, 49)))

        val arrow = slide.addPresetShape(914400, 3200000, 2000000, 1000000, PresetShapeType.RIGHT_ARROW)
        arrow.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(112, 173, 71)))

        // Image (minimal valid PNG)
        val pngData = createMinimalPng()
        slide.addPicture(4000000, 3200000, 2000000, 1500000, pngData, ImageFormat.PNG)

        // Table
        val table = slide.addTable(914400, 4800000, 7315200, 1500000, 3, 3)
        table.setCell(0, 0, "Product"); table.setCell(0, 1, "Price"); table.setCell(0, 2, "Stock")
        table.setCell(1, 0, "Widget A"); table.setCell(1, 1, "$9.99"); table.setCell(1, 2, "150")
        table.setCell(2, 0, "Widget B"); table.setCell(2, 1, "$14.99"); table.setCell(2, 2, "75")

        val file = File(context.filesDir, "shapes.pptx")
        file.outputStream().use { pres.save(it) }

        // Reload and verify
        val loaded = file.inputStream().use { Presentation.open(it) }
        val shapes = loaded.slides[0].shapes
        log.appendLine("Total shapes: ${shapes.size}")

        val presets = shapes.filterIsInstance<PresetShape>()
        check(presets.size == 3) { "Expected 3 preset shapes" }
        log.appendLine("Preset shapes: ${presets.map { it.presetType }}")
        check(presets[0].presetType == PresetShapeType.ROUND_RECT)

        val pictures = shapes.filterIsInstance<Picture>()
        check(pictures.size == 1) { "Expected 1 picture" }
        log.appendLine("Pictures: ${pictures.size} (${pictures[0].format})")

        val tables = shapes.filterIsInstance<Table>()
        check(tables.size == 1) { "Expected 1 table" }
        check(tables[0][0, 0].text == "Product") { "Table header not preserved" }
        check(tables[0][2, 1].text == "$14.99") { "Table data not preserved" }
        log.appendLine("Table: ${tables[0].rows}x${tables[0].cols}, data verified")

        return log.toString()
    }

    // -------------------------------------------------------
    // 4. Slide Operations
    // -------------------------------------------------------
    fun slideOperations(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()

        // Slide size
        pres.slideSize = SlideSize.STANDARD_4_3
        log.appendLine("Slide size: 4:3 (${pres.slideSize.widthEmu}x${pres.slideSize.heightEmu})")

        pres.addSlide("Alpha").addTextBox(0, 0, 5000000, 1000000, "Alpha content")
        pres.addSlide("Beta")
        pres.addSlide("Gamma")
        log.appendLine("Created 3 slides: ${pres.slides.map { it.name }}")

        // Move
        pres.moveSlide(2, 0) // Gamma -> first
        log.appendLine("After move(2,0): ${pres.slides.map { it.name }}")
        check(pres.slides[0].name == "Gamma")

        // Copy
        pres.copySlide(1) // Copy Alpha
        log.appendLine("After copy(1): ${pres.slideCount} slides")
        check(pres.slideCount == 4)

        // Remove
        pres.removeSlide(2) // Remove Beta
        log.appendLine("After remove(2): ${pres.slides.map { it.name }}")
        check(pres.slideCount == 3)

        // Insert
        pres.insertSlide(1, "Inserted")
        log.appendLine("After insert(1): ${pres.slides.map { it.name }}")
        check(pres.slides[1].name == "Inserted")

        // Hidden slide
        pres.slides[3].isHidden = true
        log.appendLine("Slide 4 hidden: ${pres.slides[3].isHidden}")

        // Save, reload, verify
        val file = File(context.filesDir, "slideops.pptx")
        file.outputStream().use { pres.save(it) }
        val loaded = file.inputStream().use { Presentation.open(it) }
        log.appendLine("Loaded: ${loaded.slideCount} slides")
        check(loaded.slideCount == 4) { "Expected 4 slides after reload" }
        check(loaded.slideSize == SlideSize.STANDARD_4_3) { "Slide size not preserved" }
        log.appendLine("4:3 slide size preserved: OK")

        return log.toString()
    }

    // -------------------------------------------------------
    // 5. Charts
    // -------------------------------------------------------
    fun charts(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(914400, 400000, 7315200, 600000, "Sales Chart Demo")

        // Add chart via DSL
        val chart = slide.addChart {
            type = ChartType.BAR
            title = "Quarterly Revenue"
            series("Q1", listOf(12500.0, 8700.0, 25000.0), listOf("Widgets", "Gadgets", "Services"))
            series("Q2", listOf(15300.0, 9100.0, 28000.0))
            series("Q3", listOf(14200.0, 11500.0, 32000.0))
            series("Q4", listOf(18900.0, 12300.0, 35000.0))
        }

        log.appendLine("Chart type: ${chart.type}")
        log.appendLine("Chart title: ${chart.title}")
        log.appendLine("Series count: ${chart.series.size}")
        check(chart.series.size == 4) { "Expected 4 series" }

        val file = File(context.filesDir, "charts.pptx")
        file.outputStream().use { pres.save(it) }
        log.appendLine("Saved: ${file.length()} bytes")

        return log.toString()
    }

    // -------------------------------------------------------
    // 6. Password Protection
    // -------------------------------------------------------
    fun passwordProtection(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(914400, 2000000, 7315200, 1500000, "Confidential Data")
        slide.addTextBox(914400, 3800000, 7315200, 1000000, "Revenue: $4.7M")

        // Save encrypted
        val buffer = ByteArrayOutputStream()
        pres.save(buffer, "s3cret!")
        val encrypted = buffer.toByteArray()
        log.appendLine("Encrypted size: ${encrypted.size} bytes")

        // Verify blocked without password
        var blocked = false
        try {
            Presentation.open(ByteArrayInputStream(encrypted))
        } catch (e: Exception) {
            blocked = true
        }
        check(blocked) { "Should have blocked without password" }
        log.appendLine("Blocked without password: OK")

        // Decrypt with correct password
        val loaded = Presentation.open(ByteArrayInputStream(encrypted), "s3cret!")
        check(loaded.slideCount == 1) { "Expected 1 slide" }
        val tb = loaded.slides[0].shapes[0] as TextBox
        check(tb.text == "Confidential Data") { "Content not preserved" }
        log.appendLine("Decrypted content: ${tb.text}")

        // Save decrypted copy
        val file = File(context.filesDir, "protected.pptx")
        file.outputStream().use { loaded.save(it) }
        log.appendLine("Decrypted copy saved: ${file.length()} bytes")

        return log.toString()
    }

    // -------------------------------------------------------
    // 7. HTML Export
    // -------------------------------------------------------
    fun htmlExport(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()

        // Slide 1: styled text with notes
        val s1 = pres.addSlide()
        val tb = s1.addTextBox(914400, 914400, 7315200, 1500000)
        tb.addParagraph("Export Demo", ParagraphStyle(alignment = TextAlignment.CENTER))
            .runs[0].style = TextStyle(bold = true, fontSize = 32.0, color = OfficeColor.Rgb(68, 114, 196))
        s1.notes = SpeakerNotes().apply { text = "This is the opening slide" }

        // Slide 2: table
        val s2 = pres.addSlide()
        val table = s2.addTable(914400, 914400, 7315200, 3000000, 3, 2)
        table.setCell(0, 0, "Name"); table.setCell(0, 1, "Score")
        table.setCell(1, 0, "Alice"); table.setCell(1, 1, "95")
        table.setCell(2, 0, "Bob"); table.setCell(2, 1, "87")

        // Slide 3: shapes
        val s3 = pres.addSlide()
        s3.addPresetShape(914400, 914400, 3000000, 2000000, PresetShapeType.HEART)
        s3.addTextBox(4500000, 914400, 3000000, 2000000, "日本語コンテンツ")

        val html = HtmlConverter.convert(pres, "Export Demo")
        val htmlFile = File(context.filesDir, "export.html")
        htmlFile.writeText(html)
        log.appendLine("HTML exported: ${htmlFile.length()} bytes")

        check(html.contains("<!DOCTYPE html>")) { "Missing DOCTYPE" }
        check(html.contains("<title>Export Demo</title>")) { "Missing title" }
        check(html.contains("Export Demo")) { "Missing content" }
        check(html.contains("This is the opening slide")) { "Missing notes" }
        check(html.contains("<table>")) { "Missing table" }
        check(html.contains("Alice")) { "Missing table data" }
        check(html.contains("日本語コンテンツ")) { "Missing Japanese" }
        check(html.contains("font-weight:bold")) { "Missing bold style" }
        log.appendLine("All HTML checks passed: DOCTYPE, title, content, notes, table, Japanese, styles")

        return log.toString()
    }

    // -------------------------------------------------------
    // 8. Full Presentation (All Features Combined)
    // -------------------------------------------------------
    fun fullPresentation(context: Context): String {
        val log = StringBuilder()
        val pres = Presentation()
        pres.slideSize = SlideSize.WIDESCREEN_16_9

        // --- Slide 1: Title ---
        val titleSlide = pres.addSlide("Title")
        val titleTb = titleSlide.addTextBox(914400, 1500000, 7315200, 2000000)
        titleTb.addParagraph("Q4 Business Review", ParagraphStyle(alignment = TextAlignment.CENTER))
            .runs[0].style = TextStyle(bold = true, fontSize = 44.0, fontName = "Calibri Light")
        titleTb.addParagraph("FY2026 Annual Summary", ParagraphStyle(alignment = TextAlignment.CENTER))
            .runs[0].style = TextStyle(fontSize = 20.0, color = OfficeColor.Rgb(128, 128, 128))
        titleSlide.notes = SpeakerNotes().apply { text = "Welcome everyone to the Q4 business review" }

        // --- Slide 2: Sales Data Table ---
        val data = pres.addSlide("Data")
        data.addTextBox(914400, 300000, 7315200, 600000, "Regional Sales Summary")
        val table = data.addTable(914400, 1000000, 7315200, 3500000, 5, 4)
        table.setCell(0, 0, "Region"); table.setCell(0, 1, "Q3"); table.setCell(0, 2, "Q4"); table.setCell(0, 3, "Growth")
        table.setCell(1, 0, "North"); table.setCell(1, 1, "$2.1M"); table.setCell(1, 2, "$2.8M"); table.setCell(1, 3, "+33%")
        table.setCell(2, 0, "South"); table.setCell(2, 1, "$1.5M"); table.setCell(2, 2, "$1.9M"); table.setCell(2, 3, "+27%")
        table.setCell(3, 0, "East"); table.setCell(3, 1, "$1.8M"); table.setCell(3, 2, "$2.3M"); table.setCell(3, 3, "+28%")
        table.setCell(4, 0, "Total"); table.setCell(4, 1, "$5.4M"); table.setCell(4, 2, "$7.0M"); table.setCell(4, 3, "+30%")
        data.notes = SpeakerNotes().apply { text = "Highlight 30% overall growth" }

        // --- Slide 3: Shapes + Image ---
        val visual = pres.addSlide("Visuals")
        visual.addPresetShape(914400, 800000, 3000000, 2000000, PresetShapeType.RIGHT_ARROW).apply {
            shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(112, 173, 71)))
        }
        visual.addPresetShape(4500000, 800000, 3000000, 2000000, PresetShapeType.STAR_5).apply {
            shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(255, 192, 0)))
        }
        // Embedded image
        val pngData = createMinimalPng()
        visual.addPicture(914400, 3500000, 2500000, 1800000, pngData, ImageFormat.PNG)
        // Shape with text
        val callout = visual.addPresetShape(4500000, 3500000, 3000000, 1800000, PresetShapeType.ROUND_RECT)
        callout.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(91, 155, 213)))
        callout.paragraphs.add(com.droidoffice.slide.core.Paragraph().apply {
            addRun("Key Insight", TextStyle(bold = true, fontSize = 18.0, color = OfficeColor.WHITE))
        })

        // --- Slide 4: Chart ---
        val chartSlide = pres.addSlide("Chart")
        chartSlide.addTextBox(914400, 300000, 7315200, 600000, "Revenue Trend")
        chartSlide.addChart {
            type = ChartType.BAR
            title = "Quarterly Revenue"
            series("Q1", listOf(12500.0, 8700.0, 25000.0))
            series("Q2", listOf(15300.0, 9100.0, 28000.0))
            series("Q3", listOf(14200.0, 11500.0, 32000.0))
            series("Q4", listOf(18900.0, 12300.0, 35000.0))
        }

        // --- Slide 5: Japanese ---
        val jp = pres.addSlide("日本語")
        val jpTb = jp.addTextBox(914400, 914400, 7315200, 4500000)
        jpTb.addParagraph("日本市場レポート", ParagraphStyle(alignment = TextAlignment.CENTER))
            .runs[0].style = TextStyle(bold = true, fontSize = 32.0)
        jpTb.addParagraph("第四四半期は前年同期比30%増を達成しました。")
        jpTb.addParagraph("主要指標：", ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022"))
        jpTb.addParagraph("売上高: 7.0M USD", ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022"))
        jpTb.addParagraph("顧客数: 1,200社", ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022"))

        // --- Slide 6: Hidden ---
        val hidden = pres.addSlide("Appendix")
        hidden.isHidden = true
        hidden.addTextBox(914400, 914400, 7315200, 2000000, "This slide is hidden in slideshow")

        // ===== Save =====
        val file = File(context.filesDir, "full.pptx")
        file.outputStream().use { pres.save(it) }
        log.appendLine("Saved: ${file.length()} bytes, ${pres.slideCount} slides")

        // ===== Reload and verify =====
        val loaded = file.inputStream().use { Presentation.open(it) }
        check(loaded.slideCount == 6) { "Expected 6 slides" }
        check(loaded.slideSize == SlideSize.WIDESCREEN_16_9) { "16:9 not preserved" }
        log.appendLine("Slide count: ${loaded.slideCount}, size: 16:9")

        // Title
        val titleLoaded = loaded.slides[0].shapes[0] as TextBox
        check(titleLoaded.paragraphs[0].runs[0].style.bold) { "Title bold not preserved" }
        check(titleLoaded.paragraphs[0].runs[0].style.fontSize == 44.0) { "Title font size not preserved" }
        log.appendLine("Title style: OK")

        // Notes
        check(loaded.slides[0].notes?.text == "Welcome everyone to the Q4 business review") { "Notes not preserved" }
        log.appendLine("Speaker notes: OK")

        // Table
        val loadedTable = loaded.slides[1].shapes[1] as Table
        check(loadedTable[0, 0].text == "Region") { "Table header not preserved" }
        check(loadedTable[4, 3].text == "+30%") { "Table data not preserved" }
        log.appendLine("Table (5x4): OK")

        // Shapes + picture
        val visualShapes = loaded.slides[2].shapes
        check(visualShapes.filterIsInstance<PresetShape>().size >= 2) { "Shapes not preserved" }
        check(visualShapes.filterIsInstance<Picture>().size == 1) { "Picture not preserved" }
        log.appendLine("Shapes + picture: OK")

        // Japanese
        val jpLoaded = loaded.slides[4].shapes[0] as TextBox
        check(jpLoaded.paragraphs[0].text.contains("日本市場レポート")) { "Japanese not preserved" }
        log.appendLine("Japanese content: OK")

        // ===== HTML export =====
        val html = HtmlConverter.convert(loaded, "Q4 Business Review")
        val htmlFile = File(context.filesDir, "full.html")
        htmlFile.writeText(html)
        check(html.contains("Q4 Business Review")) { "HTML title missing" }
        check(html.contains("日本市場レポート")) { "HTML Japanese missing" }
        check(html.contains("<table>")) { "HTML table missing" }
        log.appendLine("HTML export: ${htmlFile.length()} bytes, all checks passed")

        // ===== Password-protected copy =====
        val encBuffer = ByteArrayOutputStream()
        loaded.save(encBuffer, "demo123")
        val decrypted = Presentation.open(ByteArrayInputStream(encBuffer.toByteArray()), "demo123")
        check(decrypted.slideCount == 6) { "Password round-trip failed" }
        log.appendLine("Password protection: OK")

        log.appendLine("All ${loaded.slideCount} slides verified!")
        return log.toString()
    }

    // -------------------------------------------------------
    // Helper: minimal valid 1x1 white PNG
    // -------------------------------------------------------
    private fun createMinimalPng(): ByteArray {
        val header = byteArrayOf(
            0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
        )
        val ihdr = buildChunk("IHDR", byteArrayOf(
            0, 0, 0, 1, 0, 0, 0, 1, 8, 2, 0, 0, 0
        ))
        val idat = buildChunk("IDAT", byteArrayOf(
            0x78.toByte(), 0x01, 0x62, 0xF8.toByte(), 0xCF.toByte(),
            0xC0.toByte(), 0x00, 0x00, 0x00, 0x04, 0x00, 0x01
        ))
        val iend = buildChunk("IEND", byteArrayOf())
        return header + ihdr + idat + iend
    }

    private fun buildChunk(type: String, data: ByteArray): ByteArray {
        val typeBytes = type.toByteArray()
        val buf = java.nio.ByteBuffer.allocate(4 + 4 + data.size + 4)
        buf.putInt(data.size)
        buf.put(typeBytes)
        buf.put(data)
        val crc = java.util.zip.CRC32()
        crc.update(typeBytes)
        crc.update(data)
        buf.putInt(crc.value.toInt())
        return buf.array()
    }
}
