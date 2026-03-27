package com.droidoffice.slide.io

import com.droidoffice.core.exception.PasswordException
import com.droidoffice.slide.core.Presentation
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class PptxPasswordTest {

    @Test
    fun `save and open with password`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 5000, 2000, "Secret content")

        val buffer = ByteArrayOutputStream()
        pres.save(buffer, "test123")

        val loaded = Presentation.open(ByteArrayInputStream(buffer.toByteArray()), "test123")
        assertEquals(1, loaded.slideCount)
    }

    @Test
    fun `opening password-protected file without password throws`() {
        val pres = Presentation()
        pres.addSlide()
        val buffer = ByteArrayOutputStream()
        pres.save(buffer, "secret")

        assertThrows<PasswordException> {
            Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
        }
    }
}
