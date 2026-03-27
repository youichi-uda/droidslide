package com.droidoffice.slide.e2e

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.core.*
import com.droidoffice.slide.format.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Generates sample .pptx files for manual verification in LibreOffice / PowerPoint / Google Slides.
 * Output: build/generated-samples/
 */
class GenerateSamplesTest {

    companion object {
        private lateinit var outDir: File

        // EMU helpers
        private const val INCH = 914400L
        private const val CM = 360000L

        @BeforeAll
        @JvmStatic
        fun setup() {
            outDir = File("build/generated-samples")
            outDir.mkdirs()
        }
    }

    @Test
    fun `01 basic slides`() {
        val pres = Presentation()

        // Title slide
        val slide1 = pres.addSlide("Title")
        slide1.addTextBox(INCH, INCH * 2, INCH * 10, INCH, "Hello, DroidSlide!").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 44.0, bold = true)
        }
        slide1.addTextBox(INCH, INCH * 3 + INCH / 2, INCH * 10, INCH / 2, "日本語テスト — こんにちは世界！")

        // Content slide
        val slide2 = pres.addSlide("Content")
        slide2.addTextBox(INCH / 2, INCH / 2, INCH * 11, INCH / 2, "Slide 2: Content").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 28.0, bold = true)
        }
        slide2.addTextBox(INCH / 2, INCH + INCH / 2, INCH * 11, INCH * 4,
            "This is a basic DroidSlide presentation with 3 slides.\n" +
            "漢字・ひらがな・カタカナ混在テスト\n" +
            "Emoji: \uD83D\uDE00\uD83D\uDE80\uD83C\uDF1F\uD83D\uDC4D")

        // Third slide
        val slide3 = pres.addSlide("End")
        slide3.addTextBox(INCH * 3, INCH * 2, INCH * 6, INCH * 2, "Thank you!").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 48.0, bold = true, color = OfficeColor.Rgb(47, 85, 151))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }

        save(pres, "01_basic.pptx")
    }

    @Test
    fun `02 text styles`() {
        val pres = Presentation()
        val slide = pres.addSlide("Text Styles")

        slide.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Text Style Demo").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true)
        }

        val tb = slide.addTextBox(INCH / 2, INCH, INCH * 11, INCH * 5)
        tb.addParagraph("Bold text", ParagraphStyle()).runs[0].style = TextStyle(bold = true, fontSize = 20.0)
        tb.addParagraph("Italic text").runs[0].style = TextStyle(italic = true, fontSize = 20.0)
        tb.addParagraph("Underlined text").runs[0].style = TextStyle(underline = UnderlineStyle.SINGLE, fontSize = 20.0)
        tb.addParagraph("Strikethrough").runs[0].style = TextStyle(strikethrough = true, fontSize = 20.0)
        tb.addParagraph("Red 24pt Arial").runs[0].style = TextStyle(color = OfficeColor.Rgb(255, 0, 0), fontSize = 24.0, fontName = "Arial")
        tb.addParagraph("Blue 28pt").runs[0].style = TextStyle(color = OfficeColor.Rgb(0, 0, 255), fontSize = 28.0)

        // Mixed run
        val mixedPara = tb.addParagraph()
        mixedPara.addRun("Bold+", TextStyle(bold = true, fontSize = 18.0))
        mixedPara.addRun("Italic+", TextStyle(italic = true, fontSize = 18.0))
        mixedPara.addRun("Red", TextStyle(color = OfficeColor.Rgb(255, 0, 0), fontSize = 18.0))

        // Alignment
        val centered = slide.addTextBox(INCH / 2, INCH * 6, INCH * 11, INCH / 2, "Center aligned text")
        centered.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)

        save(pres, "02_text_styles.pptx")
    }

    @Test
    fun `03 shapes`() {
        val pres = Presentation()
        val slide = pres.addSlide("Shapes")

        slide.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Shape Demo").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true)
        }

        // Rectangle with fill
        val rect = slide.addPresetShape(INCH, INCH, INCH * 2, INCH, PresetShapeType.RECT)
        rect.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(47, 85, 151)),
            line = LineStyle(width = 12700, color = OfficeColor.Rgb(0, 0, 100))
        )
        rect.paragraphs.add(Paragraph().apply { addRun("Rectangle", TextStyle(color = OfficeColor.Rgb(255, 255, 255), bold = true)) })

        // Rounded rectangle
        val roundRect = slide.addPresetShape(INCH * 4, INCH, INCH * 2, INCH, PresetShapeType.ROUND_RECT)
        roundRect.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(0, 176, 80)),
            line = LineStyle(width = 12700, color = OfficeColor.Rgb(0, 100, 0))
        )
        roundRect.paragraphs.add(Paragraph().apply { addRun("Rounded", TextStyle(color = OfficeColor.Rgb(255, 255, 255))) })

        // Ellipse
        val ellipse = slide.addPresetShape(INCH * 7, INCH, INCH * 2, INCH, PresetShapeType.ELLIPSE)
        ellipse.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(255, 192, 0)),
        )
        ellipse.paragraphs.add(Paragraph().apply { addRun("Ellipse") })

        // Arrow
        val arrow = slide.addPresetShape(INCH, INCH * 3, INCH * 3, INCH, PresetShapeType.RIGHT_ARROW)
        arrow.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(200, 50, 50)),
        )
        arrow.paragraphs.add(Paragraph().apply { addRun("Arrow", TextStyle(color = OfficeColor.Rgb(255, 255, 255))) })

        // Star
        val star = slide.addPresetShape(INCH * 5, INCH * 3, INCH * 2, INCH * 2, PresetShapeType.STAR_5)
        star.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(255, 215, 0)),
        )

        // Heart
        val heart = slide.addPresetShape(INCH * 8, INCH * 3, INCH * 2, INCH * 2, PresetShapeType.HEART)
        heart.shapeStyle = ShapeStyle(
            fill = Fill.Solid(OfficeColor.Rgb(220, 20, 60)),
        )

        save(pres, "03_shapes.pptx")
    }

    @Test
    fun `04 table`() {
        val pres = Presentation()
        val slide = pres.addSlide("Table")

        slide.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Table Demo").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true)
        }

        val table = slide.addTable(INCH, INCH, INCH * 8, INCH * 3, rows = 5, cols = 4)

        // Header
        val headers = listOf("Name", "Department", "City", "Score")
        for ((c, h) in headers.withIndex()) {
            table[0, c].text = h
            table[0, c].fill = Fill.Solid(OfficeColor.Rgb(47, 85, 151))
        }

        // Data
        val data = listOf(
            listOf("田中太郎", "Engineering", "東京", "95"),
            listOf("鈴木花子", "Marketing", "大阪", "87"),
            listOf("佐藤次郎", "Sales", "名古屋", "92"),
            listOf("山田美咲", "HR", "福岡", "88"),
        )
        for ((r, row) in data.withIndex()) {
            for ((c, value) in row.withIndex()) {
                table[r + 1, c].text = value
            }
        }

        save(pres, "04_table.pptx")
    }

    @Test
    fun `05 speaker notes`() {
        val pres = Presentation()

        val slide1 = pres.addSlide("With Notes")
        slide1.addTextBox(INCH, INCH * 2, INCH * 10, INCH * 2, "This slide has speaker notes").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 36.0, bold = true)
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }
        slide1.notes = SpeakerNotes().apply {
            text = "Remember to mention the key points:\n- Revenue growth\n- Market expansion\n- Q2 targets"
        }

        val slide2 = pres.addSlide("No Notes")
        slide2.addTextBox(INCH, INCH * 2, INCH * 10, INCH, "This slide has no notes").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 28.0)
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }

        save(pres, "05_speaker_notes.pptx")
    }

    @Test
    fun `06 full presentation`() {
        val pres = Presentation()

        // --- Title slide ---
        val title = pres.addSlide("Title")
        title.addPresetShape(0, 0, pres.slideSize.widthEmu, pres.slideSize.heightEmu, PresetShapeType.RECT).also {
            it.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(31, 56, 100)))
        }
        title.addTextBox(INCH, INCH * 2, INCH * 10, INCH + INCH / 2, "Monthly Sales Report").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 44.0, bold = true, color = OfficeColor.Rgb(255, 255, 255))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }
        title.addTextBox(INCH, INCH * 4, INCH * 10, INCH / 2, "Q1 2026 Summary").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 24.0, color = OfficeColor.Rgb(180, 200, 255))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }

        // --- Overview slide ---
        val overview = pres.addSlide("Overview")
        overview.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Overview").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true, color = OfficeColor.Rgb(31, 56, 100))
        }
        val bullets = overview.addTextBox(INCH / 2, INCH, INCH * 11, INCH * 5)
        for (text in listOf("Record Q1 revenue of ¥59,550,000", "15% year-over-year growth", "Expansion into Nagoya region", "3 new distributor partnerships")) {
            bullets.addParagraph(text, ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022")).also {
                it.runs[0].style = TextStyle(fontSize = 22.0)
            }
        }

        // --- Data slide ---
        val dataSlide = pres.addSlide("Sales Data")
        dataSlide.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Sales Data").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true, color = OfficeColor.Rgb(31, 56, 100))
        }
        val table = dataSlide.addTable(INCH, INCH, INCH * 10, INCH * 3, rows = 4, cols = 4)
        val tableHeaders = listOf("Month", "Revenue", "Growth", "Region")
        for ((c, h) in tableHeaders.withIndex()) {
            table[0, c].text = h
            table[0, c].fill = Fill.Solid(OfficeColor.Rgb(47, 85, 151))
        }
        val tableData = listOf(
            listOf("January", "¥18,000,000", "+5.0%", "関東"),
            listOf("February", "¥20,250,000", "+12.5%", "関西"),
            listOf("March", "¥21,300,000", "+5.2%", "中部"),
        )
        for ((r, row) in tableData.withIndex()) {
            for ((c, v) in row.withIndex()) table[r + 1, c].text = v
        }
        dataSlide.notes = SpeakerNotes().apply {
            text = "Highlight: March revenue is the highest in company history."
        }

        // --- Next Steps slide ---
        val steps = pres.addSlide("Next Steps")
        steps.addTextBox(INCH / 2, INCH / 4, INCH * 11, INCH / 2, "Next Steps").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 32.0, bold = true, color = OfficeColor.Rgb(31, 56, 100))
        }
        val numberedList = steps.addTextBox(INCH / 2, INCH, INCH * 11, INCH * 5)
        for ((i, text) in listOf("Finalize Q2 revenue targets", "Launch Nagoya marketing campaign", "Review pricing strategy", "Prepare board presentation").withIndex()) {
            numberedList.addParagraph("${i + 1}. $text").also {
                it.runs[0].style = TextStyle(fontSize = 22.0)
            }
        }

        // --- Thank you slide ---
        val endSlide = pres.addSlide("End")
        endSlide.addPresetShape(0, 0, pres.slideSize.widthEmu, pres.slideSize.heightEmu, PresetShapeType.RECT).also {
            it.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(31, 56, 100)))
        }
        endSlide.addTextBox(INCH * 2, INCH * 2, INCH * 8, INCH * 2, "Thank You!").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 54.0, bold = true, color = OfficeColor.Rgb(255, 255, 255))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }
        endSlide.addTextBox(INCH * 2, INCH * 4 + INCH / 2, INCH * 8, INCH / 2, "droidoffice.abyo.net").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 18.0, color = OfficeColor.Rgb(180, 200, 255))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }

        save(pres, "06_full_presentation.pptx")
    }

    @Test
    fun `07 password protected`() {
        val pres = Presentation()
        val slide = pres.addSlide("Secret")
        slide.addTextBox(INCH, INCH * 2, INCH * 10, INCH * 2, "Confidential Presentation").also { tb ->
            tb.paragraphs[0].runs[0].style = TextStyle(fontSize = 36.0, bold = true, color = OfficeColor.Rgb(200, 0, 0))
            tb.paragraphs[0].style = ParagraphStyle(alignment = TextAlignment.CENTER)
        }

        val file = File(outDir, "07_password_secret123.pptx")
        file.outputStream().use { pres.save(it, "secret123") }
        println("Generated: ${file.absolutePath} (${file.length()} bytes)")
    }

    private fun save(pres: Presentation, filename: String) {
        val file = File(outDir, filename)
        file.outputStream().use { pres.save(it) }
        println("Generated: ${file.absolutePath} (${file.length()} bytes)")
    }
}
