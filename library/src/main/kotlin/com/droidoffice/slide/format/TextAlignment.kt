package com.droidoffice.slide.format

/**
 * Horizontal text alignment within a paragraph.
 * Maps to DrawingML `a:pPr algn` attribute values.
 */
enum class TextAlignment(val ooxmlValue: String) {
    LEFT("l"),
    CENTER("ctr"),
    RIGHT("r"),
    JUSTIFY("just"),
    DISTRIBUTED("dist");

    companion object {
        fun fromOoxml(value: String?): TextAlignment = when (value) {
            "l" -> LEFT
            "ctr" -> CENTER
            "r" -> RIGHT
            "just" -> JUSTIFY
            "dist" -> DISTRIBUTED
            else -> LEFT
        }
    }
}
