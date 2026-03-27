package com.droidoffice.slide.core

import com.droidoffice.core.drawingml.OfficeColor

/**
 * Presentation theme containing color scheme, fonts, and format scheme.
 */
class Theme(var name: String = "Office Theme") {
    val colorScheme = mutableMapOf<ThemeColor, OfficeColor>()
    var majorFont: String = "Calibri Light"
    var minorFont: String = "Calibri"
}

enum class ThemeColor(val ooxmlTag: String) {
    DK1("dk1"),
    LT1("lt1"),
    DK2("dk2"),
    LT2("lt2"),
    ACCENT1("accent1"),
    ACCENT2("accent2"),
    ACCENT3("accent3"),
    ACCENT4("accent4"),
    ACCENT5("accent5"),
    ACCENT6("accent6"),
    HLINK("hlink"),
    FOL_HLINK("folHlink");

    companion object {
        private val map = entries.associateBy { it.ooxmlTag }
        fun fromOoxmlTag(tag: String): ThemeColor? = map[tag]
    }
}
