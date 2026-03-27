package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PptxPhase1bRoundTripTest {

    private fun roundTrip(presentation: Presentation): Presentation {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `textbox round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(100, 200, 3000, 1500, "Hello World")

        val loaded = roundTrip(pres)
        assertEquals(1, loaded.slides[0].shapes.size)
        val tb = loaded.slides[0].shapes[0] as TextBox
        assertEquals("Hello World", tb.text)
    }

    @Test
    fun `styled text round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 5000, 2000)
        val para = tb.addParagraph()
        para.addRun("Bold", TextStyle(bold = true, fontSize = 24.0))
        para.addRun(" and ", TextStyle())
        para.addRun("Italic", TextStyle(italic = true, fontName = "Arial"))

        val loaded = roundTrip(pres)
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        assertEquals(3, loadedTb.paragraphs[0].runs.size)
        assertEquals(true, loadedTb.paragraphs[0].runs[0].style.bold)
        assertEquals(24.0, loadedTb.paragraphs[0].runs[0].style.fontSize)
        assertEquals(true, loadedTb.paragraphs[0].runs[2].style.italic)
        assertEquals("Arial", loadedTb.paragraphs[0].runs[2].style.fontName)
    }

    @Test
    fun `multiple textboxes round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 1000, 500, "First")
        slide.addTextBox(1000, 0, 1000, 500, "Second")
        slide.addTextBox(2000, 0, 1000, 500, "Third")

        val loaded = roundTrip(pres)
        assertEquals(3, loaded.slides[0].shapes.size)
        assertEquals("First", (loaded.slides[0].shapes[0] as TextBox).text)
        assertEquals("Second", (loaded.slides[0].shapes[1] as TextBox).text)
        assertEquals("Third", (loaded.slides[0].shapes[2] as TextBox).text)
    }

    @Test
    fun `japanese text round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "日本語テスト：こんにちは世界")

        val loaded = roundTrip(pres)
        val tb = loaded.slides[0].shapes[0] as TextBox
        assertEquals("日本語テスト：こんにちは世界", tb.text)
    }

    @Test
    fun `position and size round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(914400, 1828800, 4572000, 2743200, "Positioned")

        val loaded = roundTrip(pres)
        val tb = loaded.slides[0].shapes[0] as TextBox
        assertEquals(914400L, tb.x)
        assertEquals(1828800L, tb.y)
        assertEquals(4572000L, tb.width)
        assertEquals(2743200L, tb.height)
    }

    @Test
    fun `multi paragraph round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 5000, 3000)
        tb.addParagraph("First paragraph")
        tb.addParagraph("Second paragraph")
        tb.addParagraph("Third paragraph")

        val loaded = roundTrip(pres)
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        assertEquals(3, loadedTb.paragraphs.size)
        assertEquals("First paragraph", loadedTb.paragraphs[0].text)
        assertEquals("Second paragraph", loadedTb.paragraphs[1].text)
        assertEquals("Third paragraph", loadedTb.paragraphs[2].text)
    }

    @Test
    fun `text color round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 5000, 2000)
        val para = tb.addParagraph()
        para.addRun("Red text", TextStyle(color = OfficeColor.Rgb(255, 0, 0)))

        val loaded = roundTrip(pres)
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        val color = loadedTb.paragraphs[0].runs[0].style.color
        assertTrue(color is OfficeColor.Rgb)
        assertEquals(255, (color as OfficeColor.Rgb).red)
        assertEquals(0, color.green)
        assertEquals(0, color.blue)
    }

    @Test
    fun `paragraph alignment round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 5000, 2000)
        tb.addParagraph("Centered", ParagraphStyle(alignment = TextAlignment.CENTER))

        val loaded = roundTrip(pres)
        val loadedTb = loaded.slides[0].shapes[0] as TextBox
        assertEquals(TextAlignment.CENTER, loadedTb.paragraphs[0].style.alignment)
    }
}
