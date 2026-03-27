package com.droidoffice.slide.core

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.format.BulletType
import com.droidoffice.slide.format.ParagraphStyle
import com.droidoffice.slide.format.TextAlignment
import com.droidoffice.slide.format.TextStyle
import com.droidoffice.slide.format.UnderlineStyle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TextBoxTest {

    @Test
    fun `paragraphs and runs`() {
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val para = tb.addParagraph()
        para.addRun("Hello ")
        para.addRun("World")
        assertEquals("Hello World", para.text)
        assertEquals(1, tb.paragraphs.size)
        assertEquals(2, para.runs.size)
    }

    @Test
    fun `bold and italic style`() {
        val style = TextStyle(bold = true, italic = true)
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val para = tb.addParagraph()
        para.addRun("Styled", style)
        assertEquals(true, para.runs[0].style.bold)
        assertEquals(true, para.runs[0].style.italic)
    }

    @Test
    fun `font name and size`() {
        val style = TextStyle(fontName = "Arial", fontSize = 24.0)
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val para = tb.addParagraph()
        para.addRun("Test", style)
        assertEquals("Arial", para.runs[0].style.fontName)
        assertEquals(24.0, para.runs[0].style.fontSize)
    }

    @Test
    fun `text color`() {
        val style = TextStyle(color = OfficeColor.Rgb(255, 0, 0))
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val para = tb.addParagraph()
        para.addRun("Red", style)
        val color = para.runs[0].style.color
        assertEquals(OfficeColor.Rgb(255, 0, 0), color)
    }

    @Test
    fun `paragraph alignment`() {
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val para = tb.addParagraph("Centered", ParagraphStyle(alignment = TextAlignment.CENTER))
        assertEquals(TextAlignment.CENTER, para.style.alignment)
    }

    @Test
    fun `bullet list`() {
        val tb = TextBox(1, "tb", 0, 0, 1000, 500)
        val style = ParagraphStyle(bulletType = BulletType.CHAR, bulletChar = "\u2022")
        val para = tb.addParagraph("Item 1", style)
        assertEquals(BulletType.CHAR, para.style.bulletType)
        assertEquals("\u2022", para.style.bulletChar)
    }
}
