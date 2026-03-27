package com.droidoffice.slide.core

/**
 * Base class for all shapes on a slide.
 * Positions and dimensions are in EMU (English Metric Units).
 */
abstract class Shape {
    abstract val id: Int
    abstract val name: String
    abstract var x: Long
    abstract var y: Long
    abstract var width: Long
    abstract var height: Long
    abstract var rotation: Double
}
