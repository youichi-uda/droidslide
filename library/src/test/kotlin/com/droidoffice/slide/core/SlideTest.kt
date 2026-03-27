package com.droidoffice.slide.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SlideTest {

    @Test
    fun `add textbox to slide`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(100, 200, 3000, 1500, "Hello")
        assertEquals(1, slide.shapes.size)
        assertTrue(slide.shapes[0] is TextBox)
        assertEquals("Hello", (slide.shapes[0] as TextBox).text)
        assertEquals(100L, tb.x)
        assertEquals(200L, tb.y)
    }

    @Test
    fun `textbox text getter`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val tb = slide.addTextBox(0, 0, 1000, 500)
        tb.addParagraph("Line 1")
        tb.addParagraph("Line 2")
        assertEquals("Line 1\nLine 2", tb.text)
    }
}
