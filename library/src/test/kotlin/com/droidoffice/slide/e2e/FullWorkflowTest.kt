package com.droidoffice.slide.e2e

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.chart.ChartType
import com.droidoffice.slide.convert.HtmlConverter
import com.droidoffice.slide.core.PresetShapeType
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.SpeakerNotes
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.ShapeStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FullWorkflowTest {

    private fun roundTrip(presentation: Presentation): Presentation {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `presentation with notes round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "Main content")
        slide.notes = SpeakerNotes().apply { text = "Talk about X and Y" }

        val loaded = roundTrip(pres)
        assertEquals("Talk about X and Y", loaded.slides[0].notes?.text)
    }

    @Test
    fun `full sales presentation workflow`() {
        val pres = Presentation()

        // Title slide
        val title = pres.addSlide("Title")
        val titleTb = title.addTextBox(914400, 2000000, 7315200, 1500000)
        titleTb.addParagraph("Q4 Sales Report", ParagraphStyle(alignment = TextAlignment.CENTER))
        title.notes = SpeakerNotes().apply { text = "Welcome everyone" }

        // Data slide with table
        val data = pres.addSlide("Data")
        val table = data.addTable(914400, 1200000, 7315200, 3000000, 3, 3)
        table.setCell(0, 0, "Region")
        table.setCell(0, 1, "Q3")
        table.setCell(0, 2, "Q4")
        table.setCell(1, 0, "East")
        table.setCell(1, 1, "$1.2M")
        table.setCell(1, 2, "$1.5M")
        table.setCell(2, 0, "West")
        table.setCell(2, 1, "$0.8M")
        table.setCell(2, 2, "$1.1M")

        // Shapes slide
        val shapes = pres.addSlide("Shapes")
        val rect = shapes.addPresetShape(914400, 1200000, 3000000, 2000000, PresetShapeType.ROUND_RECT)
        rect.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(68, 114, 196)))

        // Save, reload, verify, export to HTML
        val loaded = roundTrip(pres)
        assertEquals(3, loaded.slideCount)
        assertEquals("Welcome everyone", loaded.slides[0].notes?.text)

        val html = HtmlConverter.convert(loaded, "Q4 Sales Report")
        assertTrue(html.contains("Q4 Sales Report"))
        assertTrue(html.contains("East"))
    }

    @Test
    fun `japanese content workflow`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(914400, 1200000, 7315200, 3000000)
        tb.addParagraph("売上レポート", ParagraphStyle(alignment = TextAlignment.CENTER))
        tb.addParagraph("第四四半期の成果")
        slide.notes = SpeakerNotes().apply { text = "日本語ノート" }

        val loaded = roundTrip(pres)
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        assertEquals("売上レポート", loadedTb.paragraphs[0].text)
        assertEquals("第四四半期の成果", loadedTb.paragraphs[1].text)
        assertEquals("日本語ノート", loaded.slides[0].notes?.text)
    }

    @Test
    fun `password protected workflow`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "Confidential data")

        val buffer = ByteArrayOutputStream()
        pres.save(buffer, "secret123")

        val loaded = Presentation.open(ByteArrayInputStream(buffer.toByteArray()), "secret123")
        val tb = loaded.slides[0].shapes[0] as TextBox
        assertEquals("Confidential data", tb.text)
    }

    @Test
    fun `multi-feature presentation`() {
        val pres = Presentation()

        // Slide 1: styled text
        val s1 = pres.addSlide()
        val tb = s1.addTextBox(914400, 914400, 7315200, 1500000)
        val para = tb.addParagraph()
        para.addRun("Bold Red", TextStyle(bold = true, color = OfficeColor.Rgb(255, 0, 0), fontSize = 32.0))
        para.addRun(" Normal", TextStyle(fontSize = 18.0))

        // Slide 2: shapes + table
        val s2 = pres.addSlide()
        s2.addPresetShape(100, 100, 3000000, 2000000, PresetShapeType.ELLIPSE)
        val table = s2.addTable(100, 2500000, 6000000, 2000000, 2, 2)
        table.setCell(0, 0, "A")
        table.setCell(1, 1, "D")

        // Slide 3: notes
        val s3 = pres.addSlide()
        s3.addTextBox(100, 100, 5000000, 3000000, "Final slide")
        s3.notes = SpeakerNotes().apply { text = "Thank the audience" }

        val loaded = roundTrip(pres)
        assertEquals(3, loaded.slideCount)

        // Verify HTML export works
        val html = HtmlConverter.convert(loaded)
        assertTrue(html.contains("Bold Red"))
        assertTrue(html.contains("Final slide"))
        assertTrue(html.contains("Thank the audience"))
    }
}
