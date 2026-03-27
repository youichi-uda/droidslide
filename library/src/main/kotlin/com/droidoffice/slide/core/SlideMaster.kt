package com.droidoffice.slide.core

/**
 * A slide master containing default styles and layout references.
 */
class SlideMaster(val name: String = "") {
    internal val layouts = mutableListOf<SlideLayout>()
    internal var themeRId: String = ""
    val placeholders = mutableListOf<Placeholder>()
}
