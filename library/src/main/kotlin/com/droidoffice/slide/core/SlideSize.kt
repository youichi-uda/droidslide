package com.droidoffice.slide.core

/**
 * Slide dimensions in EMU (English Metric Units).
 * 1 inch = 914400 EMU, 1 cm = 360000 EMU, 1 pt = 12700 EMU.
 */
data class SlideSize(val widthEmu: Long, val heightEmu: Long) {
    companion object {
        /** 16:9 widescreen (default for modern PowerPoint). */
        val WIDESCREEN_16_9 = SlideSize(12_192_000, 6_858_000)

        /** 4:3 standard. */
        val STANDARD_4_3 = SlideSize(9_144_000, 6_858_000)
    }
}
