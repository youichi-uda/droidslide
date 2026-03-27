package com.droidoffice.slide.io

import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.SlideSize
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class PptxRoundTripTest {

    private fun roundTrip(presentation: Presentation): Presentation {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `empty presentation round trip`() {
        val pres = Presentation()
        pres.addSlide()
        val loaded = roundTrip(pres)
        assertEquals(1, loaded.slideCount)
    }

    @Test
    fun `multiple slides round trip`() {
        val pres = Presentation()
        pres.addSlide("Slide 1")
        pres.addSlide("Slide 2")
        pres.addSlide("Slide 3")
        val loaded = roundTrip(pres)
        assertEquals(3, loaded.slideCount)
    }

    @Test
    fun `slide size round trip with 16x9`() {
        val pres = Presentation()
        pres.slideSize = SlideSize.WIDESCREEN_16_9
        pres.addSlide()
        val loaded = roundTrip(pres)
        assertEquals(SlideSize.WIDESCREEN_16_9, loaded.slideSize)
    }

    @Test
    fun `slide size round trip with 4x3`() {
        val pres = Presentation()
        pres.slideSize = SlideSize.STANDARD_4_3
        pres.addSlide()
        val loaded = roundTrip(pres)
        assertEquals(SlideSize.STANDARD_4_3, loaded.slideSize)
    }
}
