package com.droidoffice.slide.core

/**
 * Speaker notes attached to a slide.
 * Maps to `ppt/notesSlides/notesSlide{n}.xml`.
 */
class SpeakerNotes {
    val paragraphs = mutableListOf<Paragraph>()

    var text: String
        get() = paragraphs.joinToString("\n") { it.text }
        set(value) {
            paragraphs.clear()
            val para = Paragraph()
            para.addRun(value)
            paragraphs.add(para)
        }
}
