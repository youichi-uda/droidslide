package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.core.ooxml.OoxmlPackage
import com.droidoffice.core.ooxml.parseRelationships
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.ThemeColor
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PptxLayoutMasterTest {

    private fun roundTrip(presentation: Presentation): Presentation {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    private fun saveToPkg(presentation: Presentation): OoxmlPackage {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return OoxmlPackage.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `default master and layout exist in generated pptx`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)
        assertNotNull(pkg.getPart("ppt/slideMasters/slideMaster1.xml"))
        assertNotNull(pkg.getPart("ppt/slideLayouts/slideLayout1.xml"))
        assertNotNull(pkg.getPart("ppt/theme/theme1.xml"))
    }

    @Test
    fun `theme round trip preserves color scheme`() {
        val pres = Presentation()
        pres.addSlide()

        val loaded = roundTrip(pres)
        val theme = loaded.theme
        assertEquals("Office Theme", theme.name)
        // Default theme should have 12 colors
        assertTrue(theme.colorScheme.isNotEmpty(), "Color scheme should not be empty")
    }

    @Test
    fun `theme color scheme reads dk1 and lt1`() {
        val pres = Presentation()
        pres.addSlide()

        val loaded = roundTrip(pres)
        val dk1 = loaded.theme.colorScheme[ThemeColor.DK1]
        val lt1 = loaded.theme.colorScheme[ThemeColor.LT1]
        assertNotNull(dk1, "DK1 should be parsed")
        assertNotNull(lt1, "LT1 should be parsed")
        // Default: dk1 = black (000000), lt1 = white (FFFFFF)
        assertTrue(dk1 is OfficeColor.Rgb)
        assertTrue(lt1 is OfficeColor.Rgb)
    }

    @Test
    fun `theme accent colors are parsed`() {
        val pres = Presentation()
        pres.addSlide()
        val loaded = roundTrip(pres)
        val accent1 = loaded.theme.colorScheme[ThemeColor.ACCENT1]
        assertNotNull(accent1, "ACCENT1 should be parsed")
        // Default Office theme accent1 = 4472C4
        assertTrue(accent1 is OfficeColor.Rgb)
        assertEquals(68, (accent1 as OfficeColor.Rgb).red)  // 0x44
    }

    @Test
    fun `theme font round trip`() {
        val pres = Presentation()
        pres.addSlide()

        val loaded = roundTrip(pres)
        assertEquals("Calibri Light", loaded.theme.majorFont)
        assertEquals("Calibri", loaded.theme.minorFont)
    }

    @Test
    fun `slide references layout via rels`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)
        val slideRels = pkg.getPartAsStream("ppt/slides/_rels/slide1.xml.rels")
        assertNotNull(slideRels)
        val rels = parseRelationships(slideRels)
        val layoutRel = rels.find { it.target.contains("slideLayout") }
        assertNotNull(layoutRel, "Slide should reference a layout")
    }

    @Test
    fun `master references theme via rels`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)
        val masterRels = pkg.getPartAsStream("ppt/slideMasters/_rels/slideMaster1.xml.rels")
        assertNotNull(masterRels)
        val rels = parseRelationships(masterRels)
        val themeRel = rels.find { it.target.contains("theme") }
        assertNotNull(themeRel, "Master should reference a theme")
    }

    @Test
    fun `master references layout via rels`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)
        val masterRels = pkg.getPartAsStream("ppt/slideMasters/_rels/slideMaster1.xml.rels")
        assertNotNull(masterRels)
        val rels = parseRelationships(masterRels)
        val layoutRel = rels.find { it.target.contains("slideLayout") }
        assertNotNull(layoutRel, "Master should reference a layout")
    }

    @Test
    fun `generated pptx has master layout theme parts`() {
        val pres = Presentation()
        pres.addSlide()
        pres.addSlide()
        val pkg = saveToPkg(pres)
        val contentTypes = String(pkg.getPart("[Content_Types].xml")!!)
        assertTrue(contentTypes.contains("slideMaster+xml"))
        assertTrue(contentTypes.contains("slideLayout+xml"))
        assertTrue(contentTypes.contains("theme+xml"))
    }

    @Test
    fun `relationship chain slide to layout to master to theme`() {
        val pres = Presentation()
        pres.addSlide()
        val pkg = saveToPkg(pres)

        // Slide -> Layout
        val slideRels = parseRelationships(pkg.getPartAsStream("ppt/slides/_rels/slide1.xml.rels")!!)
        assertTrue(slideRels.any { it.target.contains("slideLayout") })

        // Layout -> Master
        val layoutRels = parseRelationships(pkg.getPartAsStream("ppt/slideLayouts/_rels/slideLayout1.xml.rels")!!)
        assertTrue(layoutRels.any { it.target.contains("slideMaster") })

        // Master -> Theme
        val masterRels = parseRelationships(pkg.getPartAsStream("ppt/slideMasters/_rels/slideMaster1.xml.rels")!!)
        assertTrue(masterRels.any { it.target.contains("theme") })
    }
}
