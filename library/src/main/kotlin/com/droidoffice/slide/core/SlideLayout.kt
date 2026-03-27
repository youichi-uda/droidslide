package com.droidoffice.slide.core

/**
 * A slide layout definition (e.g., Title, Blank, Two Content).
 */
class SlideLayout(
    val name: String = "",
    val type: LayoutType = LayoutType.BLANK,
) {
    internal var masterRId: String = ""
    val placeholders = mutableListOf<Placeholder>()
}

enum class LayoutType(val ooxmlValue: String) {
    TITLE("title"),
    TITLE_AND_CONTENT("obj"),
    SECTION_HEADER("secHead"),
    TWO_CONTENT("twoObj"),
    COMPARISON("twoTxTwoObj"),
    TITLE_ONLY("titleOnly"),
    BLANK("blank"),
    CONTENT_WITH_CAPTION("objTx"),
    PICTURE_WITH_CAPTION("picTx");

    companion object {
        private val map = entries.associateBy { it.ooxmlValue }
        fun fromOoxml(value: String?): LayoutType = map[value] ?: BLANK
    }
}
