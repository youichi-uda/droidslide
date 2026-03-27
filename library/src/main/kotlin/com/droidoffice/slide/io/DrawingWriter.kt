package com.droidoffice.slide.io

import com.droidoffice.core.ooxml.OoxmlPackage
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.Shape
import com.droidoffice.slide.core.Slide

/**
 * Writes media files (images) and their relationships for slides.
 */
object DrawingWriter {

    /**
     * Writes image media parts and returns updated slide relationships XML.
     * Also assigns rId to each Picture shape.
     */
    fun writeMedia(
        pkg: OoxmlPackage,
        slideIndex: Int,
        slide: Slide,
    ): String {
        val pictures = slide.shapes.filterIsInstance<Picture>()
        if (pictures.isEmpty()) return buildSlideRels(emptyList())

        var nextRId = 2  // rId1 = slideLayout
        val picRels = mutableListOf<PicRelEntry>()

        for ((imgIndex, pic) in pictures.withIndex()) {
            val rId = "rId${nextRId++}"
            val mediaNum = imgIndex + 1
            val mediaPath = "ppt/media/image${slideIndex + 1}_$mediaNum.${pic.format.extension}"
            pic.rId = rId
            pic.mediaPath = mediaPath
            pkg.setPart(mediaPath, pic.imageData)
            picRels.add(PicRelEntry(rId, "../media/image${slideIndex + 1}_$mediaNum.${pic.format.extension}"))
        }

        // Update SlideWriter's nextRelId so it doesn't conflict
        SlideWriter.nextRelId = nextRId

        return buildSlideRels(picRels)
    }

    private data class PicRelEntry(val rId: String, val target: String)

    private fun buildSlideRels(picRels: List<PicRelEntry>): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>""")
        for (rel in picRels) {
            appendLine("""  <Relationship Id="${rel.rId}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image" Target="${rel.target}"/>""")
        }
        appendLine("</Relationships>")
    }
}
