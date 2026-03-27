package com.droidoffice.slide.io

import com.droidoffice.core.ooxml.OoxmlPackage
import com.droidoffice.slide.core.Picture
import com.droidoffice.slide.core.Presentation
import com.droidoffice.slide.core.Slide
import java.io.OutputStream

/**
 * Writes a Presentation to .pptx format (OOXML PresentationML).
 */
object PptxWriter {

    fun write(presentation: Presentation, output: OutputStream) {
        val pkg = OoxmlPackage.create()

        pkg.setPart("[Content_Types].xml", buildContentTypes(presentation).toByteArray())
        pkg.setPart("_rels/.rels", buildTopRels().toByteArray())
        pkg.setPart("ppt/presentation.xml", buildPresentation(presentation).toByteArray())
        pkg.setPart("ppt/_rels/presentation.xml.rels", buildPresentationRels(presentation).toByteArray())

        // Default theme, master, layout (required for valid pptx)
        pkg.setPart("ppt/theme/theme1.xml", buildDefaultTheme().toByteArray())
        pkg.setPart("ppt/slideMasters/slideMaster1.xml", buildDefaultSlideMaster().toByteArray())
        pkg.setPart("ppt/slideMasters/_rels/slideMaster1.xml.rels", buildSlideMasterRels().toByteArray())
        pkg.setPart("ppt/slideLayouts/slideLayout1.xml", buildDefaultSlideLayout().toByteArray())
        pkg.setPart("ppt/slideLayouts/_rels/slideLayout1.xml.rels", buildSlideLayoutRels().toByteArray())

        // Write each slide (media must be written before slide XML so rIds are assigned)
        for ((index, slide) in presentation.slides.withIndex()) {
            val slideNum = index + 1
            val hasPictures = slide.shapes.any { it is Picture }
            val slideRelsXml = if (hasPictures) {
                DrawingWriter.writeMedia(pkg, index, slide)
            } else {
                buildSlideRels()
            }
            // Notes support: add notes relationship to slide rels if notes exist
            val notesRels = mutableListOf<String>()
            if (slide.notes != null && slide.notes!!.text.isNotBlank()) {
                val notesRId = "rId${SlideWriter.nextRelId++}"
                notesRels.add("""  <Relationship Id="$notesRId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/notesSlide" Target="../notesSlides/notesSlide$slideNum.xml"/>""")
                pkg.setPart("ppt/notesSlides/notesSlide$slideNum.xml", buildNotesSlide(slide, slideNum).toByteArray())
            }

            // Build final slide rels with added notes
            val finalSlideRels = if (notesRels.isNotEmpty()) {
                slideRelsXml.replace("</Relationships>", notesRels.joinToString("\n") + "\n</Relationships>")
            } else slideRelsXml

            pkg.setPart("ppt/slides/slide$slideNum.xml", buildSlide(slide).toByteArray())
            pkg.setPart("ppt/slides/_rels/slide$slideNum.xml.rels", finalSlideRels.toByteArray())
        }

        pkg.writeTo(output)
    }

    private fun buildContentTypes(presentation: Presentation): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        appendLine("""  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        appendLine("""  <Default Extension="xml" ContentType="application/xml"/>""")
        appendLine("""  <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>""")
        appendLine("""  <Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>""")
        appendLine("""  <Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>""")
        appendLine("""  <Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>""")
        for (i in presentation.slides.indices) {
            appendLine("""  <Override PartName="/ppt/slides/slide${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>""")
        }
        // Notes content types
        for ((i, slide) in presentation.slides.withIndex()) {
            if (slide.notes != null && slide.notes!!.text.isNotBlank()) {
                appendLine("""  <Override PartName="/ppt/notesSlides/notesSlide${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.notesSlide+xml"/>""")
            }
        }
        // Image content types
        val imageFormats = presentation.slides.flatMap { s -> s.shapes.filterIsInstance<Picture>() }
            .map { it.format }.toSet()
        for (fmt in imageFormats) {
            appendLine("""  <Default Extension="${fmt.extension}" ContentType="${fmt.contentType}"/>""")
        }
        appendLine("</Types>")
    }

    private fun buildTopRels(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/>""")
        appendLine("</Relationships>")
    }

    private fun buildPresentation(presentation: Presentation): String = buildString {
        val sz = presentation.slideSize
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<p:presentation xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">""")

        // Slide master ID list
        val masterRId = "rId${presentation.slides.size + 1}"
        appendLine("""  <p:sldMasterIdLst>""")
        appendLine("""    <p:sldMasterId id="2147483648" r:id="$masterRId"/>""")
        appendLine("""  </p:sldMasterIdLst>""")

        // Slide ID list
        appendLine("  <p:sldIdLst>")
        for ((index, slide) in presentation.slides.withIndex()) {
            val id = slide.slideId.takeIf { it > 0 } ?: (256 + index)
            appendLine("""    <p:sldId id="$id" r:id="rId${index + 1}"/>""")
        }
        appendLine("  </p:sldIdLst>")

        appendLine("""  <p:sldSz cx="${sz.widthEmu}" cy="${sz.heightEmu}"/>""")
        appendLine("""  <p:notesSz cx="${sz.heightEmu}" cy="${sz.widthEmu}"/>""")
        appendLine("</p:presentation>")
    }

    private fun buildPresentationRels(presentation: Presentation): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")

        // Slide relationships
        for (i in presentation.slides.indices) {
            appendLine("""  <Relationship Id="rId${i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide${i + 1}.xml"/>""")
        }

        // Slide master
        val masterRId = presentation.slides.size + 1
        appendLine("""  <Relationship Id="rId$masterRId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>""")

        // Theme
        val themeRId = masterRId + 1
        appendLine("""  <Relationship Id="rId$themeRId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="theme/theme1.xml"/>""")

        appendLine("</Relationships>")
    }

    internal fun buildSlide(slide: Slide): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<p:sld xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">""")
        appendLine("  <p:cSld>")
        appendLine("    <p:spTree>")
        appendLine("""      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>""")
        appendLine("""      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>""")
        append(SlideWriter.writeShapes(slide.shapes))
        appendLine("    </p:spTree>")
        appendLine("  </p:cSld>")
        appendLine("</p:sld>")
    }

    private fun buildSlideRels(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>""")
        appendLine("</Relationships>")
    }

    private fun buildDefaultTheme(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="Office Theme">""")
        appendLine("  <a:themeElements>")
        appendLine("    <a:clrScheme name=\"Office\">")
        appendLine("""      <a:dk1><a:sysClr val="windowText" lastClr="000000"/></a:dk1>""")
        appendLine("""      <a:lt1><a:sysClr val="window" lastClr="FFFFFF"/></a:lt1>""")
        appendLine("""      <a:dk2><a:srgbClr val="44546A"/></a:dk2>""")
        appendLine("""      <a:lt2><a:srgbClr val="E7E6E6"/></a:lt2>""")
        appendLine("""      <a:accent1><a:srgbClr val="4472C4"/></a:accent1>""")
        appendLine("""      <a:accent2><a:srgbClr val="ED7D31"/></a:accent2>""")
        appendLine("""      <a:accent3><a:srgbClr val="A5A5A5"/></a:accent3>""")
        appendLine("""      <a:accent4><a:srgbClr val="FFC000"/></a:accent4>""")
        appendLine("""      <a:accent5><a:srgbClr val="5B9BD5"/></a:accent5>""")
        appendLine("""      <a:accent6><a:srgbClr val="70AD47"/></a:accent6>""")
        appendLine("""      <a:hlink><a:srgbClr val="0563C1"/></a:hlink>""")
        appendLine("""      <a:folHlink><a:srgbClr val="954F72"/></a:folHlink>""")
        appendLine("    </a:clrScheme>")
        appendLine("""    <a:fontScheme name="Office">""")
        appendLine("      <a:majorFont><a:latin typeface=\"Calibri Light\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:majorFont>")
        appendLine("      <a:minorFont><a:latin typeface=\"Calibri\"/><a:ea typeface=\"\"/><a:cs typeface=\"\"/></a:minorFont>")
        appendLine("    </a:fontScheme>")
        appendLine("""    <a:fmtScheme name="Office">""")
        appendLine("      <a:fillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:fillStyleLst>")
        appendLine("      <a:lnStyleLst><a:ln w=\"6350\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln><a:ln w=\"12700\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln><a:ln w=\"19050\"><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:ln></a:lnStyleLst>")
        appendLine("      <a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst>")
        appendLine("      <a:bgFillStyleLst><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill><a:solidFill><a:schemeClr val=\"phClr\"/></a:solidFill></a:bgFillStyleLst>")
        appendLine("    </a:fmtScheme>")
        appendLine("  </a:themeElements>")
        appendLine("</a:theme>")
    }

    private fun buildDefaultSlideMaster(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<p:sldMaster xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">""")
        appendLine("  <p:cSld>")
        appendLine("    <p:spTree>")
        appendLine("""      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>""")
        appendLine("""      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>""")
        appendLine("    </p:spTree>")
        appendLine("  </p:cSld>")
        appendLine("  <p:clrMap bg1=\"lt1\" tx1=\"dk1\" bg2=\"lt2\" tx2=\"dk2\" accent1=\"accent1\" accent2=\"accent2\" accent3=\"accent3\" accent4=\"accent4\" accent5=\"accent5\" accent6=\"accent6\" hlink=\"hlink\" folHlink=\"folHlink\"/>")
        appendLine("  <p:sldLayoutIdLst>")
        appendLine("""    <p:sldLayoutId id="2147483649" r:id="rId1"/>""")
        appendLine("  </p:sldLayoutIdLst>")
        appendLine("</p:sldMaster>")
    }

    private fun buildSlideMasterRels(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/>""")
        appendLine("""  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/>""")
        appendLine("</Relationships>")
    }

    private fun buildDefaultSlideLayout(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<p:sldLayout xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main" type="blank">""")
        appendLine("  <p:cSld name=\"Blank\">")
        appendLine("    <p:spTree>")
        appendLine("""      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>""")
        appendLine("""      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>""")
        appendLine("    </p:spTree>")
        appendLine("  </p:cSld>")
        appendLine("</p:sldLayout>")
    }

    private fun buildSlideLayoutRels(): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        appendLine("""  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/>""")
        appendLine("</Relationships>")
    }

    private fun buildNotesSlide(slide: Slide, slideNum: Int): String = buildString {
        appendLine("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        appendLine("""<p:notes xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships" xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main">""")
        appendLine("  <p:cSld>")
        appendLine("    <p:spTree>")
        appendLine("""      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>""")
        appendLine("""      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>""")
        // Notes body placeholder
        appendLine("      <p:sp>")
        appendLine("        <p:nvSpPr>")
        appendLine("""          <p:cNvPr id="2" name="Notes Placeholder"/>""")
        appendLine("""          <p:cNvSpPr><a:spLocks noGrp="1"/></p:cNvSpPr>""")
        appendLine("""          <p:nvPr><p:ph type="body" idx="1"/></p:nvPr>""")
        appendLine("        </p:nvSpPr>")
        appendLine("        <p:spPr/>")
        appendLine("        <p:txBody>")
        appendLine("""          <a:bodyPr/>""")
        appendLine("""          <a:lstStyle/>""")
        val notes = slide.notes
        if (notes != null) {
            for (para in notes.paragraphs) {
                append("          <a:p>")
                for (run in para.runs) {
                    append("<a:r><a:rPr lang=\"en-US\" dirty=\"0\"/><a:t>${escapeXml(run.text)}</a:t></a:r>")
                }
                if (para.runs.isEmpty()) append("<a:endParaRPr/>")
                appendLine("</a:p>")
            }
        }
        appendLine("        </p:txBody>")
        appendLine("      </p:sp>")
        appendLine("    </p:spTree>")
        appendLine("  </p:cSld>")
        appendLine("</p:notes>")
    }

    internal fun escapeXml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
