package com.droidoffice.slide.format

/**
 * Paragraph-level formatting properties.
 * Maps to DrawingML `a:pPr` element.
 */
data class ParagraphStyle(
    /** Horizontal alignment. */
    var alignment: TextAlignment = TextAlignment.LEFT,
    /** Indent level (0-8). */
    var level: Int = 0,
    /** Line spacing in percent (e.g. 150.0 = 150%). Null = default. */
    var lineSpacing: Double? = null,
    /** Space before paragraph in points. */
    var spaceBefore: Double? = null,
    /** Space after paragraph in points. */
    var spaceAfter: Double? = null,
    /** Bullet type. */
    var bulletType: BulletType = BulletType.NONE,
    /** Bullet character (for CHAR type). */
    var bulletChar: String? = null,
)

enum class BulletType {
    NONE,
    CHAR,
    AUTO_NUMBERED,
}
