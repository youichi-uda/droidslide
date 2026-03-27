package com.droidoffice.slide.core

/**
 * A hyperlink on a text run or shape.
 */
data class Hyperlink(
    val url: String = "",
    val slideIndex: Int? = null,
    val tooltip: String? = null,
)
