package com.droidoffice.slide.format

import com.droidoffice.core.drawingml.OfficeColor

/**
 * Line/outline style for shapes.
 * Maps to DrawingML `a:ln` element.
 */
data class LineStyle(
    /** Width in EMU. */
    var width: Long = 0,
    /** Line color. */
    var color: OfficeColor? = null,
    /** Dash pattern. */
    var dashStyle: LineDash = LineDash.SOLID,
)

enum class LineDash(val ooxmlValue: String) {
    SOLID("solid"),
    DASH("dash"),
    DOT("dot"),
    DASH_DOT("dashDot"),
    LONG_DASH("lgDash"),
    LONG_DASH_DOT("lgDashDot");

    companion object {
        fun fromOoxml(value: String?): LineDash = entries.find { it.ooxmlValue == value } ?: SOLID
    }
}
