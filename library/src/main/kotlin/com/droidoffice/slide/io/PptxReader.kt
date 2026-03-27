package com.droidoffice.slide.io

import com.droidoffice.core.exception.InvalidFileException
import com.droidoffice.core.ooxml.OoxmlPackage
import com.droidoffice.core.ooxml.RelationshipTypes
import com.droidoffice.core.ooxml.SaxReader
import com.droidoffice.core.ooxml.parseRelationships
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.Slide
import com.droidoffice.slide.core.SlideSize
import com.droidoffice.slide.core.SpeakerNotes
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream

/**
 * Reads .pptx files (OOXML PresentationML) using SAX streaming.
 */
object PptxReader {

    fun read(input: InputStream): Presentation {
        val pkg = OoxmlPackage.open(input)
        val presentation = Presentation()

        // 1. Read presentation.xml to get slide list and slide size
        val slideEntries = pkg.getPartAsStream("ppt/presentation.xml")?.let { stream ->
            readPresentation(stream, presentation)
        } ?: throw InvalidFileException("Missing ppt/presentation.xml")

        // 2. Read presentation relationships to map rId -> slide paths
        val rels = pkg.getPartAsStream("ppt/_rels/presentation.xml.rels")?.let { stream ->
            parseRelationships(stream)
        } ?: emptyList()

        val rIdToTarget = rels.filter { it.type == RelationshipTypes.SLIDE }
            .associate { it.id to it.target }

        // 2b. Read theme
        val themeRel = rels.find { it.type == RelationshipTypes.THEME }
        if (themeRel != null) {
            val themePath = "ppt/${themeRel.target}"
            pkg.getPartAsStream(themePath)?.let { stream ->
                presentation.theme = ThemeReader.read(stream)
            }
        }

        // 3. Read each slide in order
        for ((slideId, rId) in slideEntries) {
            val target = rIdToTarget[rId] ?: continue
            val partPath = "ppt/$target"
            val slideDir = partPath.substringBeforeLast("/")

            val slide = Slide()
            slide.slideId = slideId
            slide.rId = rId

            // Read slide relationships (for images etc.)
            val slideFileName = target.substringAfterLast("/")
            val slideRels = pkg.getPartAsStream("$slideDir/_rels/$slideFileName.rels")?.let { s ->
                parseRelationships(s)
            } ?: emptyList()
            val slideRIdToTarget = slideRels.associate { it.id to it.target }

            // Set up media loader for images
            SlideReader.mediaLoader = { imgRId ->
                val imgTarget = slideRIdToTarget[imgRId]
                if (imgTarget != null) {
                    val imgPath = if (imgTarget.startsWith("../")) {
                        "ppt/${imgTarget.removePrefix("../")}"
                    } else {
                        "$slideDir/$imgTarget"
                    }
                    val data = pkg.getPart(imgPath)
                    if (data != null) {
                        val ext = imgPath.substringAfterLast(".")
                        data to ext
                    } else null
                } else null
            }

            pkg.getPartAsStream(partPath)?.let { stream ->
                SlideReader.read(stream, slide)
            }

            SlideReader.mediaLoader = null

            // Read notes if present
            val notesRel = slideRels.find { it.type == RelationshipTypes.NOTE_SLIDE }
            if (notesRel != null) {
                val notesTarget = notesRel.target
                val notesPath = if (notesTarget.startsWith("../")) {
                    "ppt/${notesTarget.removePrefix("../")}"
                } else {
                    "$slideDir/$notesTarget"
                }
                pkg.getPartAsStream(notesPath)?.let { stream ->
                    readNotes(stream, slide)
                }
            }

            presentation.addSlideInternal(slide)
        }

        return presentation
    }

    /**
     * Parses presentation.xml. Returns list of (slideId, rId) in order.
     */
    private fun readPresentation(
        input: InputStream,
        presentation: Presentation,
    ): List<SlideEntry> {
        val entries = mutableListOf<SlideEntry>()

        SaxReader.parse(input, object : DefaultHandler() {
            override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                when (localName) {
                    "sldSz" -> {
                        val cx = attributes.getValue("cx")?.toLongOrNull() ?: return
                        val cy = attributes.getValue("cy")?.toLongOrNull() ?: return
                        presentation.slideSize = SlideSize(cx, cy)
                    }
                    "sldId" -> {
                        val id = attributes.getValue("id")?.toIntOrNull() ?: return
                        val rId = attributes.getValue(
                            "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
                            "id"
                        ) ?: attributes.getValue("r:id") ?: return
                        entries.add(SlideEntry(id, rId))
                    }
                }
            }
        })

        // Track next slide ID
        val maxId = entries.maxOfOrNull { it.slideId } ?: 255
        presentation.nextSlideId = maxId + 1

        return entries
    }

    internal data class SlideEntry(val slideId: Int, val rId: String)

    private fun readNotes(input: InputStream, slide: Slide) {
        val notes = SpeakerNotes()
        val textParts = mutableListOf<String>()

        SaxReader.parse(input, object : DefaultHandler() {
            private var inBody = false
            private var inParagraph = false
            private var inRun = false
            private var inText = false
            private val textBuffer = StringBuilder()

            override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
                when (localName) {
                    "ph" -> {
                        val type = attributes.getValue("type")
                        if (type == "body") inBody = true
                    }
                    "p" -> if (inBody) inParagraph = true
                    "r" -> if (inParagraph) inRun = true
                    "t" -> if (inRun) { inText = true; textBuffer.clear() }
                }
            }

            override fun characters(ch: CharArray, start: Int, length: Int) {
                if (inText) textBuffer.append(ch, start, length)
            }

            override fun endElement(uri: String, localName: String, qName: String) {
                when (localName) {
                    "t" -> if (inText) { textParts.add(textBuffer.toString()); inText = false }
                    "r" -> inRun = false
                    "p" -> inParagraph = false
                    "txBody" -> inBody = false
                }
            }
        })

        if (textParts.isNotEmpty()) {
            notes.text = textParts.joinToString("")
            slide.notes = notes
        }
    }

    private fun readSlide(input: InputStream, slide: Slide) {
        SlideReader.read(input, slide)
    }
}
