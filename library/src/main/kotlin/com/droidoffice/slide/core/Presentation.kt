package com.droidoffice.slide.core

import com.droidoffice.slide.io.PptxReader
import com.droidoffice.slide.io.PptxWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * The main entry point for working with PowerPoint presentations.
 */
class Presentation {

    private val _slides = mutableListOf<Slide>()

    /** All slides in this presentation. */
    val slides: List<Slide> get() = _slides

    /** Slide dimensions. */
    var slideSize: SlideSize = SlideSize.WIDESCREEN_16_9

    /** Theme applied to this presentation. */
    var theme: Theme = Theme()

    /** Slide masters. */
    internal val masters = mutableListOf<SlideMaster>()

    /** Slide layouts. */
    internal val layouts = mutableListOf<SlideLayout>()

    internal var nextSlideId: Int = 256

    // -- Slide operations --

    fun addSlide(name: String = ""): Slide {
        val slide = Slide(name)
        slide.slideId = nextSlideId++
        _slides.add(slide)
        return slide
    }

    fun insertSlide(index: Int, name: String = ""): Slide {
        val slide = Slide(name)
        slide.slideId = nextSlideId++
        _slides.add(index, slide)
        return slide
    }

    fun removeSlide(index: Int) {
        _slides.removeAt(index)
    }

    fun moveSlide(fromIndex: Int, toIndex: Int) {
        val slide = _slides.removeAt(fromIndex)
        _slides.add(toIndex, slide)
    }

    fun copySlide(sourceIndex: Int): Slide {
        val source = _slides[sourceIndex]
        val copy = Slide(source.name)
        copy.slideId = nextSlideId++
        copy.isHidden = source.isHidden
        copy.layoutRId = source.layoutRId
        // Deep copy of shapes is handled per shape type in later phases
        _slides.add(copy)
        return copy
    }

    val slideCount: Int get() = _slides.size

    /** Used by PptxReader to add pre-constructed slides. */
    internal fun addSlideInternal(slide: Slide) {
        _slides.add(slide)
    }

    // -- I/O --

    fun save(output: OutputStream) {
        PptxWriter.write(this, output)
    }

    fun save(output: OutputStream, password: String) {
        val buffer = java.io.ByteArrayOutputStream()
        PptxWriter.write(this, buffer)
        val encrypted = com.droidoffice.core.ooxml.EncryptedPackage.encrypt(buffer.toByteArray(), password)
        output.write(encrypted)
    }

    suspend fun saveAsync(output: OutputStream) {
        withContext(Dispatchers.IO) { save(output) }
    }

    suspend fun saveAsync(output: OutputStream, password: String) {
        withContext(Dispatchers.IO) { save(output, password) }
    }

    companion object {
        fun open(input: InputStream): Presentation {
            val bytes = input.readBytes()
            return if (com.droidoffice.core.ooxml.EncryptedPackage.isEncrypted(bytes)) {
                throw com.droidoffice.core.exception.PasswordException(
                    "This file is password-protected. Use open(input, password) instead."
                )
            } else {
                PptxReader.read(java.io.ByteArrayInputStream(bytes))
            }
        }

        fun open(input: InputStream, password: String): Presentation {
            val bytes = input.readBytes()
            val decrypted = if (com.droidoffice.core.ooxml.EncryptedPackage.isEncrypted(bytes)) {
                com.droidoffice.core.ooxml.EncryptedPackage.decrypt(bytes, password)
            } else {
                bytes
            }
            return PptxReader.read(java.io.ByteArrayInputStream(decrypted))
        }

        suspend fun openAsync(input: InputStream): Presentation {
            return withContext(Dispatchers.IO) { open(input) }
        }

        suspend fun openAsync(input: InputStream, password: String): Presentation {
            return withContext(Dispatchers.IO) { open(input, password) }
        }
    }
}
