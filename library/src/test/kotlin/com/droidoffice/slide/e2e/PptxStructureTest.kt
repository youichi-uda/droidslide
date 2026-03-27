package com.droidoffice.slide.e2e

import com.droidoffice.core.ooxml.OoxmlPackage
import com.droidoffice.slide.core.Presentation
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PptxStructureTest {

    private fun saveToPkg(presentation: Presentation): OoxmlPackage {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return OoxmlPackage.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `minimal pptx has required parts`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)

        assertNotNull(pkg.getPart("[Content_Types].xml"), "Missing [Content_Types].xml")
        assertNotNull(pkg.getPart("_rels/.rels"), "Missing _rels/.rels")
        assertNotNull(pkg.getPart("ppt/presentation.xml"), "Missing ppt/presentation.xml")
        assertNotNull(pkg.getPart("ppt/_rels/presentation.xml.rels"), "Missing presentation.xml.rels")
        assertNotNull(pkg.getPart("ppt/slides/slide1.xml"), "Missing slide1.xml")
        assertNotNull(pkg.getPart("ppt/slides/_rels/slide1.xml.rels"), "Missing slide1.xml.rels")
        assertNotNull(pkg.getPart("ppt/slideMasters/slideMaster1.xml"), "Missing slideMaster1.xml")
        assertNotNull(pkg.getPart("ppt/slideLayouts/slideLayout1.xml"), "Missing slideLayout1.xml")
        assertNotNull(pkg.getPart("ppt/theme/theme1.xml"), "Missing theme1.xml")
    }

    @Test
    fun `Content_Types contains correct PresentationML types`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)

        val contentTypes = String(pkg.getPart("[Content_Types].xml")!!)
        assertTrue(contentTypes.contains("presentationml.presentation.main+xml"), "Missing presentation content type")
        assertTrue(contentTypes.contains("presentationml.slide+xml"), "Missing slide content type")
        assertTrue(contentTypes.contains("presentationml.slideMaster+xml"), "Missing slideMaster content type")
        assertTrue(contentTypes.contains("presentationml.slideLayout+xml"), "Missing slideLayout content type")
        assertTrue(contentTypes.contains("theme+xml"), "Missing theme content type")
    }
}
