package com.droidoffice.slide.convert

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.SpeakerNotes
import com.droidoffice.slide.format.TextStyle
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class HtmlConverterTest {

    @Test
    fun `convert simple presentation`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "Hello World")

        val html = HtmlConverter.convert(pres, "Test")
        assertTrue(html.contains("Hello World"))
        assertTrue(html.contains("<title>Test</title>"))
        assertTrue(html.contains("<!DOCTYPE html>"))
    }

    @Test
    fun `convert styled text`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 5000, 2000)
        val para = tb.addParagraph()
        para.addRun("Bold", TextStyle(bold = true, color = OfficeColor.Rgb(255, 0, 0)))

        val html = HtmlConverter.convert(pres)
        assertTrue(html.contains("font-weight:bold"))
        assertTrue(html.contains("color:#FF0000"))
        assertTrue(html.contains("Bold"))
    }

    @Test
    fun `convert japanese content`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "日本語テスト")

        val html = HtmlConverter.convert(pres)
        assertTrue(html.contains("日本語テスト"))
        assertTrue(html.contains("charset=\"UTF-8\""))
    }

    @Test
    fun `convert multi-slide presentation`() {
        val pres = Presentation()
        pres.addSlide().addTextBox(0, 0, 5000, 2000, "Slide 1")
        pres.addSlide().addTextBox(0, 0, 5000, 2000, "Slide 2")
        pres.addSlide().addTextBox(0, 0, 5000, 2000, "Slide 3")

        val html = HtmlConverter.convert(pres)
        assertTrue(html.contains("Slide 1"))
        assertTrue(html.contains("Slide 2"))
        assertTrue(html.contains("Slide 3"))
        assertTrue(html.contains("Slide 1</div>") || html.contains("Slide 1"))
    }

    @Test
    fun `convert presentation with notes`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "Content")
        slide.notes = SpeakerNotes().apply { text = "Remember to mention X" }

        val html = HtmlConverter.convert(pres)
        assertTrue(html.contains("Remember to mention X"))
        assertTrue(html.contains("Notes:"))
    }

    @Test
    fun `convert presentation with table`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val table = slide.addTable(0, 0, 5000, 2000, 2, 2)
        table.setCell(0, 0, "A1")
        table.setCell(0, 1, "B1")
        table.setCell(1, 0, "A2")
        table.setCell(1, 1, "B2")

        val html = HtmlConverter.convert(pres)
        assertTrue(html.contains("<table>"))
        assertTrue(html.contains("A1"))
        assertTrue(html.contains("B2"))
    }
}
