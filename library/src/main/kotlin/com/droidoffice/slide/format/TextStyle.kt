package com.droidoffice.slide.format

import com.droidoffice.core.drawingml.OfficeColor

/**
 * Text run formatting properties.
 * Maps to DrawingML `a:rPr` element attributes.
 */
data class TextStyle(
    /** Font face name (a:latin typeface). */
    var fontName: String? = null,
    /** Font size in points (a:rPr sz is in hundredths of a point). */
    var fontSize: Double? = null,
    /** Bold (a:rPr b). */
    var bold: Boolean = false,
    /** Italic (a:rPr i). */
    var italic: Boolean = false,
    /** Underline style (a:rPr u). */
    var underline: UnderlineStyle = UnderlineStyle.NONE,
    /** Strikethrough (a:rPr strike). */
    var strikethrough: Boolean = false,
    /** Text color. */
    var color: OfficeColor? = null,
)

enum class UnderlineStyle(val ooxmlValue: String) {
    NONE("none"),
    SINGLE("sng"),
    DOUBLE("dbl"),
    HEAVY("heavy"),
    WAVY("wavy"),
    DOTTED("dotted"),
    DASHED("dash");

    companion object {
        fun fromOoxml(value: String?): UnderlineStyle = entries.find { it.ooxmlValue == value } ?: NONE
    }
}
