package com.droidoffice.slide.core

/**
 * A placeholder shape within a slide layout or master.
 */
data class Placeholder(
    val type: PlaceholderType,
    val index: Int = 0,
    var x: Long = 0,
    var y: Long = 0,
    var width: Long = 0,
    var height: Long = 0,
)

enum class PlaceholderType(val ooxmlValue: String) {
    TITLE("title"),
    CENTERED_TITLE("ctrTitle"),
    SUBTITLE("subTitle"),
    BODY("body"),
    DATE("dt"),
    FOOTER("ftr"),
    SLIDE_NUMBER("sldNum");

    companion object {
        private val map = entries.associateBy { it.ooxmlValue }
        fun fromOoxml(value: String?): PlaceholderType? = map[value]
    }
}
