package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.core.ImageFormat
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.PresetShape
import com.droidoffice.slide.core.PresetShapeType
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.Table
import com.droidoffice.slide.core.TextBox
import com.droidoffice.slide.format.LineStyle
import com.droidoffice.slide.format.ShapeStyle
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PptxShapeRoundTripTest {

    private fun roundTrip(presentation: Presentation): Presentation {
        val buffer = ByteArrayOutputStream()
        presentation.save(buffer)
        return Presentation.open(ByteArrayInputStream(buffer.toByteArray()))
    }

    @Test
    fun `preset shape round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val shape = slide.addPresetShape(100, 200, 3000, 2000, PresetShapeType.ELLIPSE)
        shape.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(0, 128, 255)))

        val loaded = roundTrip(pres)
        val loadedShape = loaded.slides[0].shapes[0]
        assertTrue(loadedShape is PresetShape)
        assertEquals(PresetShapeType.ELLIPSE, loadedShape.presetType)
    }

    @Test
    fun `picture round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        // Create a minimal 1x1 PNG
        val pngData = createMinimalPng()
        slide.addPicture(100, 200, 3000, 2000, pngData, ImageFormat.PNG)

        val loaded = roundTrip(pres)
        assertEquals(1, loaded.slides[0].shapes.size)
        val pic = loaded.slides[0].shapes[0]
        assertTrue(pic is Picture)
        assertEquals(100L, pic.x)
        assertEquals(200L, pic.y)
    }

    @Test
    fun `table round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val table = slide.addTable(100, 200, 6000, 3000, 2, 3)
        table.setCell(0, 0, "A1")
        table.setCell(0, 1, "B1")
        table.setCell(0, 2, "C1")
        table.setCell(1, 0, "A2")
        table.setCell(1, 1, "B2")
        table.setCell(1, 2, "C2")

        val loaded = roundTrip(pres)
        val loadedTable = loaded.slides[0].shapes[0]
        assertTrue(loadedTable is Table)
        assertEquals(2, loadedTable.rows)
        assertEquals(3, loadedTable.cols)
        assertEquals("A1", loadedTable[0, 0].text)
        assertEquals("C2", loadedTable[1, 2].text)
    }

    @Test
    fun `multiple shape types on one slide`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 3000, 1000, "Text")
        slide.addPresetShape(0, 1000, 3000, 1000, PresetShapeType.RECT)
        slide.addTable(0, 2000, 6000, 1000, 1, 2)

        val loaded = roundTrip(pres)
        assertEquals(3, loaded.slides[0].shapes.size)
        assertTrue(loaded.slides[0].shapes[0] is TextBox)
        assertTrue(loaded.slides[0].shapes[1] is PresetShape)
        assertTrue(loaded.slides[0].shapes[2] is Table)
    }

    @Test
    fun `shape position and size preserved`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addPresetShape(914400, 1828800, 4572000, 2743200, PresetShapeType.RECT)

        val loaded = roundTrip(pres)
        val s = loaded.slides[0].shapes[0]
        assertEquals(914400L, s.x)
        assertEquals(1828800L, s.y)
        assertEquals(4572000L, s.width)
        assertEquals(2743200L, s.height)
    }

    @Test
    fun `shape rotation round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val shape = slide.addPresetShape(0, 0, 2000, 1000, PresetShapeType.RECT)
        shape.rotation = 45.0

        val loaded = roundTrip(pres)
        val loadedShape = loaded.slides[0].shapes[0] as PresetShape
        assertEquals(45.0, loadedShape.rotation, 0.01)
    }

    @Test
    fun `table cell text round trip`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val table = slide.addTable(0, 0, 4000, 2000, 2, 2)
        table.setCell(0, 0, "Header 1")
        table.setCell(0, 1, "Header 2")
        table.setCell(1, 0, "Data 1")
        table.setCell(1, 1, "Data 2")

        val loaded = roundTrip(pres)
        val t = loaded.slides[0].shapes[0] as Table
        assertEquals("Header 1", t[0, 0].text)
        assertEquals("Header 2", t[0, 1].text)
        assertEquals("Data 1", t[1, 0].text)
        assertEquals("Data 2", t[1, 1].text)
    }

    @Test
    fun `add and remove shapes`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        slide.addTextBox(0, 0, 1000, 500, "Keep")
        slide.addTextBox(0, 0, 1000, 500, "Remove")
        slide.addTextBox(0, 0, 1000, 500, "Keep2")
        assertEquals(3, slide.shapes.size)
        slide.removeShape(1)
        assertEquals(2, slide.shapes.size)
        assertEquals("Keep", (slide.shapes[0] as TextBox).text)
        assertEquals("Keep2", (slide.shapes[1] as TextBox).text)
    }

    companion object {
        /** Creates a minimal valid 1x1 white PNG file. */
        fun createMinimalPng(): ByteArray {
            val header = byteArrayOf(
                0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
            )
            // IHDR chunk: 1x1, 8-bit RGB
            val ihdr = buildChunk("IHDR", byteArrayOf(
                0, 0, 0, 1,  // width
                0, 0, 0, 1,  // height
                8,            // bit depth
                2,            // color type (RGB)
                0, 0, 0       // compression, filter, interlace
            ))
            // IDAT chunk: minimal compressed data for 1 white pixel
            val idat = buildChunk("IDAT", byteArrayOf(
                0x78.toByte(), 0x01, 0x62, 0xF8.toByte(), 0xCF.toByte(),
                0xC0.toByte(), 0x00, 0x00, 0x00, 0x04, 0x00, 0x01
            ))
            // IEND chunk
            val iend = buildChunk("IEND", byteArrayOf())
            return header + ihdr + idat + iend
        }

        private fun buildChunk(type: String, data: ByteArray): ByteArray {
            val length = data.size
            val typeBytes = type.toByteArray()
            val buf = java.nio.ByteBuffer.allocate(4 + 4 + data.size + 4)
            buf.putInt(length)
            buf.put(typeBytes)
            buf.put(data)
            // CRC (simplified - use 0 for test purposes)
            val crc = java.util.zip.CRC32()
            crc.update(typeBytes)
            crc.update(data)
            buf.putInt(crc.value.toInt())
            return buf.array()
        }
    }
}
