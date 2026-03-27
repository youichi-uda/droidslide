package com.droidoffice.slide.core

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PresentationTest {

    @Test
    fun `add slide increments count`() {
        val pres = Presentation()
        assertEquals(0, pres.slideCount)
        pres.addSlide("Slide 1")
        assertEquals(1, pres.slideCount)
        pres.addSlide("Slide 2")
        assertEquals(2, pres.slideCount)
    }

    @Test
    fun `insert slide at index`() {
        val pres = Presentation()
        pres.addSlide("A")
        pres.addSlide("C")
        pres.insertSlide(1, "B")
        assertEquals(3, pres.slideCount)
        assertEquals("A", pres.slides[0].name)
        assertEquals("B", pres.slides[1].name)
        assertEquals("C", pres.slides[2].name)
    }

    @Test
    fun `remove slide`() {
        val pres = Presentation()
        pres.addSlide("A")
        pres.addSlide("B")
        pres.addSlide("C")
        pres.removeSlide(1)
        assertEquals(2, pres.slideCount)
        assertEquals("A", pres.slides[0].name)
        assertEquals("C", pres.slides[1].name)
    }

    @Test
    fun `move slide`() {
        val pres = Presentation()
        pres.addSlide("A")
        pres.addSlide("B")
        pres.addSlide("C")
        pres.moveSlide(0, 2)
        assertEquals("B", pres.slides[0].name)
        assertEquals("C", pres.slides[1].name)
        assertEquals("A", pres.slides[2].name)
    }

    @Test
    fun `copy slide`() {
        val pres = Presentation()
        val original = pres.addSlide("Original")
        original.isHidden = true
        val copy = pres.copySlide(0)
        assertEquals(2, pres.slideCount)
        assertEquals("Original", copy.name)
        assertEquals(true, copy.isHidden)
        // Copy should have different slide ID
        assert(copy.slideId != original.slideId)
    }
}
