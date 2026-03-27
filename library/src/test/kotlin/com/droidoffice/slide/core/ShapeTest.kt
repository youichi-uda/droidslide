package com.droidoffice.slide.core

import com.droidoffice.core.drawingml.Fill
import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.slide.format.LineStyle
import com.droidoffice.slide.format.ShapeStyle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShapeTest {

    @Test
    fun `preset shape types map correctly`() {
        assertEquals("rect", PresetShapeType.RECT.ooxmlValue)
        assertEquals("ellipse", PresetShapeType.ELLIPSE.ooxmlValue)
        assertEquals("rightArrow", PresetShapeType.RIGHT_ARROW.ooxmlValue)
        assertEquals(PresetShapeType.RECT, PresetShapeType.fromOoxml("rect"))
        assertEquals(PresetShapeType.ELLIPSE, PresetShapeType.fromOoxml("ellipse"))
    }

    @Test
    fun `shape with solid fill`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val shape = slide.addPresetShape(0, 0, 2000, 1000, PresetShapeType.RECT)
        shape.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(0, 0, 255)))
        assertTrue(shape.shapeStyle.fill is Fill.Solid)
    }

    @Test
    fun `shape with line style`() {
        val style = ShapeStyle(line = LineStyle(width = 12700, color = OfficeColor.Rgb(255, 0, 0)))
        assertEquals(12700L, style.line.width)
    }

    @Test
    fun `preset shape with text`() {
        val pres = Presentation()
        val slide = pres.addSlide()
        val shape = slide.addPresetShape(0, 0, 2000, 1000, PresetShapeType.ROUND_RECT)
        val para = Paragraph()
        para.addRun("Shape text")
        shape.paragraphs.add(para)
        assertEquals("Shape text", shape.text)
    }
}
