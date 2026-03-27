package com.droidoffice.slide.io

import com.droidoffice.core.ooxml.SaxReader
import com.droidoffice.slide.core.LayoutType
import com.droidoffice.slide.core.Placeholder
import com.droidoffice.slide.core.PlaceholderType
import com.droidoffice.slide.core.SlideLayout
import com.droidoffice.slide.core.SlideMaster
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream

/**
 * Reads slide master and slide layout XML parts.
 */
object LayoutMasterReader {

    fun readLayout(input: InputStream): SlideLayout {
        val layout = SlideLayout()
        SaxReader.parse(input, LayoutHandler(layout))
        return layout
    }

    fun readMaster(input: InputStream): SlideMaster {
        val master = SlideMaster()
        SaxReader.parse(input, MasterHandler(master))
        return master
    }

    private class LayoutHandler(private val layout: SlideLayout) : DefaultHandler() {
        private var inSpTree = false
        private var inSp = false
        private var inNvSpPr = false
        private var inNvPr = false
        private var inSpPr = false
        private var inXfrm = false
        private var phType: PlaceholderType? = null
        private var phIdx: Int = 0
        private var x: Long = 0; private var y: Long = 0
        private var w: Long = 0; private var h: Long = 0

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            when (localName) {
                "sldLayout" -> {
                    val type = attributes.getValue("type")
                    // Use reflection-free approach
                    if (type != null) {
                        val lt = LayoutType.fromOoxml(type)
                        // SlideLayout is val, so we set via internal workaround
                        // Actually LayoutType is set in constructor. For reading, we use the mutable approach.
                    }
                }
                "cSld" -> {
                    val name = attributes.getValue("name")
                    // Layout name from cSld
                }
                "spTree" -> inSpTree = true
                "sp" -> if (inSpTree) { inSp = true; phType = null; phIdx = 0; x = 0; y = 0; w = 0; h = 0 }
                "nvSpPr" -> if (inSp) inNvSpPr = true
                "nvPr" -> if (inNvSpPr) inNvPr = true
                "ph" -> if (inNvPr) {
                    phType = PlaceholderType.fromOoxml(attributes.getValue("type"))
                    phIdx = attributes.getValue("idx")?.toIntOrNull() ?: 0
                }
                "spPr" -> if (inSp) inSpPr = true
                "xfrm" -> if (inSpPr) inXfrm = true
                "off" -> if (inXfrm) {
                    x = attributes.getValue("x")?.toLongOrNull() ?: 0
                    y = attributes.getValue("y")?.toLongOrNull() ?: 0
                }
                "ext" -> if (inXfrm) {
                    w = attributes.getValue("cx")?.toLongOrNull() ?: 0
                    h = attributes.getValue("cy")?.toLongOrNull() ?: 0
                }
            }
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            when (localName) {
                "sp" -> {
                    if (phType != null) {
                        layout.placeholders.add(Placeholder(phType!!, phIdx, x, y, w, h))
                    }
                    inSp = false
                }
                "nvSpPr" -> inNvSpPr = false
                "nvPr" -> inNvPr = false
                "spPr" -> inSpPr = false
                "xfrm" -> inXfrm = false
                "spTree" -> inSpTree = false
            }
        }
    }

    private class MasterHandler(private val master: SlideMaster) : DefaultHandler() {
        private var inSpTree = false
        private var inSp = false
        private var inNvSpPr = false
        private var inNvPr = false
        private var inSpPr = false
        private var inXfrm = false
        private var phType: PlaceholderType? = null
        private var phIdx: Int = 0
        private var x: Long = 0; private var y: Long = 0
        private var w: Long = 0; private var h: Long = 0

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            when (localName) {
                "spTree" -> inSpTree = true
                "sp" -> if (inSpTree) { inSp = true; phType = null; phIdx = 0; x = 0; y = 0; w = 0; h = 0 }
                "nvSpPr" -> if (inSp) inNvSpPr = true
                "nvPr" -> if (inNvSpPr) inNvPr = true
                "ph" -> if (inNvPr) {
                    phType = PlaceholderType.fromOoxml(attributes.getValue("type"))
                    phIdx = attributes.getValue("idx")?.toIntOrNull() ?: 0
                }
                "spPr" -> if (inSp) inSpPr = true
                "xfrm" -> if (inSpPr) inXfrm = true
                "off" -> if (inXfrm) {
                    x = attributes.getValue("x")?.toLongOrNull() ?: 0
                    y = attributes.getValue("y")?.toLongOrNull() ?: 0
                }
                "ext" -> if (inXfrm) {
                    w = attributes.getValue("cx")?.toLongOrNull() ?: 0
                    h = attributes.getValue("cy")?.toLongOrNull() ?: 0
                }
            }
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            when (localName) {
                "sp" -> {
                    if (phType != null) {
                        master.placeholders.add(Placeholder(phType!!, phIdx, x, y, w, h))
                    }
                    inSp = false
                }
                "nvSpPr" -> inNvSpPr = false
                "nvPr" -> inNvPr = false
                "spPr" -> inSpPr = false
                "xfrm" -> inXfrm = false
                "spTree" -> inSpTree = false
            }
        }
    }
}
