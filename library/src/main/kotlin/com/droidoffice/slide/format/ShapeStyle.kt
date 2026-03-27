package com.droidoffice.slide.format

import com.droidoffice.core.drawingml.Fill

/**
 * Visual style for shapes (fill, outline, effects).
 */
data class ShapeStyle(
    var fill: Fill = Fill.None,
    var line: LineStyle = LineStyle(),
)
