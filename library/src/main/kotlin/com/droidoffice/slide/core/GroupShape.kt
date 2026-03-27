package com.droidoffice.slide.core

/**
 * A group of shapes that act as a single unit.
 * Maps to `p:grpSp` in OOXML.
 */
class GroupShape(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
) : Shape() {
    private val _children = mutableListOf<Shape>()
    val children: List<Shape> get() = _children

    fun addChild(shape: Shape) {
        _children.add(shape)
    }
}
