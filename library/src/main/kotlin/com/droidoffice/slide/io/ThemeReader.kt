package com.droidoffice.slide.io

import com.droidoffice.core.drawingml.OfficeColor
import com.droidoffice.core.ooxml.SaxReader
import com.droidoffice.slide.core.Theme
import com.droidoffice.slide.core.ThemeColor
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream

/**
 * Reads theme XML (`ppt/theme/theme{n}.xml`).
 */
object ThemeReader {

    fun read(input: InputStream): Theme {
        val theme = Theme()
        SaxReader.parse(input, ThemeHandler(theme))
        return theme
    }

    private class ThemeHandler(private val theme: Theme) : DefaultHandler() {

        private var inClrScheme = false
        private var inFontScheme = false
        private var inMajorFont = false
        private var inMinorFont = false
        private var currentColorTag: String? = null
        private var themeName: String? = null

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            when (localName) {
                "theme" -> {
                    themeName = attributes.getValue("name")
                    if (themeName != null) theme.name = themeName!!
                }
                "clrScheme" -> {
                    inClrScheme = true
                    attributes.getValue("name")?.let { }
                }
                "fontScheme" -> inFontScheme = true
                "majorFont" -> if (inFontScheme) inMajorFont = true
                "minorFont" -> if (inFontScheme) inMinorFont = true
                "latin" -> {
                    val typeface = attributes.getValue("typeface") ?: return
                    when {
                        inMajorFont -> theme.majorFont = typeface
                        inMinorFont -> theme.minorFont = typeface
                    }
                }
                // Color scheme children: dk1, lt1, dk2, lt2, accent1-6, hlink, folHlink
                else -> if (inClrScheme) {
                    val tc = ThemeColor.fromOoxmlTag(localName)
                    if (tc != null) {
                        currentColorTag = localName
                    }
                    // Parse color value
                    if (currentColorTag != null) {
                        when (localName) {
                            "srgbClr" -> {
                                val hex = attributes.getValue("val")
                                if (hex != null) {
                                    val tc2 = ThemeColor.fromOoxmlTag(currentColorTag!!)
                                    if (tc2 != null) {
                                        theme.colorScheme[tc2] = OfficeColor.Rgb.fromHex(hex)
                                    }
                                }
                            }
                            "sysClr" -> {
                                val lastClr = attributes.getValue("lastClr")
                                if (lastClr != null) {
                                    val tc2 = ThemeColor.fromOoxmlTag(currentColorTag!!)
                                    if (tc2 != null) {
                                        theme.colorScheme[tc2] = OfficeColor.Rgb.fromHex(lastClr)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            when (localName) {
                "clrScheme" -> { inClrScheme = false; currentColorTag = null }
                "fontScheme" -> inFontScheme = false
                "majorFont" -> inMajorFont = false
                "minorFont" -> inMinorFont = false
                else -> if (inClrScheme && ThemeColor.fromOoxmlTag(localName) != null) {
                    currentColorTag = null
                }
            }
        }
    }
}
