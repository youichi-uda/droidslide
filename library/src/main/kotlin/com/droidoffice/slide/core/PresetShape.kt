package com.droidoffice.slide.core

import com.droidoffice.slide.format.ShapeStyle

/**
 * A shape with a preset geometry (rectangle, ellipse, arrow, etc.).
 * Maps to `p:sp` with `a:prstGeom` in OOXML.
 */
class PresetShape(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
    val presetType: PresetShapeType,
) : Shape() {
    val paragraphs = mutableListOf<Paragraph>()
    var shapeStyle: ShapeStyle = ShapeStyle()

    val text: String get() = paragraphs.joinToString("\n") { it.text }
}

/**
 * Preset shape types. Maps to OOXML `a:prstGeom prst` values.
 */
enum class PresetShapeType(val ooxmlValue: String) {
    RECT("rect"),
    ROUND_RECT("roundRect"),
    ELLIPSE("ellipse"),
    TRIANGLE("triangle"),
    RIGHT_TRIANGLE("rtTriangle"),
    DIAMOND("diamond"),
    TRAPEZOID("trapezoid"),
    PARALLELOGRAM("parallelogram"),
    PENTAGON("pentagon"),
    HEXAGON("hexagon"),
    STAR_5("star5"),
    STAR_6("star6"),
    HEART("heart"),
    CLOUD("cloud"),
    RIGHT_ARROW("rightArrow"),
    LEFT_ARROW("leftArrow"),
    UP_ARROW("upArrow"),
    DOWN_ARROW("downArrow"),
    LINE("line"),
    CALLOUT_1("callout1"),
    CALLOUT_2("callout2");

    companion object {
        private val map = entries.associateBy { it.ooxmlValue }
        fun fromOoxml(value: String?): PresetShapeType? = map[value]
    }
}
