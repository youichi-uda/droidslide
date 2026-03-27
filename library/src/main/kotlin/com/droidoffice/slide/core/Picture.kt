package com.droidoffice.slide.core

/**
 * An image embedded in a slide.
 * Maps to `p:pic` in OOXML.
 */
class Picture(
    override val id: Int,
    override val name: String,
    override var x: Long,
    override var y: Long,
    override var width: Long,
    override var height: Long,
    override var rotation: Double = 0.0,
    val imageData: ByteArray,
    val format: ImageFormat,
) : Shape() {
    internal var mediaPath: String = ""
    internal var rId: String = ""
}

enum class ImageFormat(val extension: String, val contentType: String) {
    PNG("png", "image/png"),
    JPEG("jpeg", "image/jpeg"),
    GIF("gif", "image/gif"),
    BMP("bmp", "image/bmp"),
    WEBP("webp", "image/webp"),
}
