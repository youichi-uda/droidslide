# DroidSlide

**Android PowerPoint (.pptx) Library** — Create, read, and modify PowerPoint presentations on Android with pure Kotlin.

[![](https://jitpack.io/v/com.droidoffice/droidslide.svg)](https://jitpack.io/#com.droidoffice/droidslide)

## Features

- **Read & Write .pptx** — Full OOXML PresentationML support
- **Slides** — Add, remove, move, copy, hide slides
- **Text** — TextBox with paragraphs, runs, fonts, colors, alignment, bullets
- **Shapes** — 20+ preset shapes (rect, ellipse, arrow, star, etc.) with fill and line styles
- **Images** — Embed PNG, JPEG, GIF, BMP, WebP
- **Tables** — Create and populate tables with styled cells
- **Speaker Notes** — Read and write presenter notes
- **Charts** — Bar, Line, Pie, Area, Scatter charts (DSL builder)
- **Theme** — Color scheme, fonts (read/write)
- **Layouts & Masters** — Slide layout and master support
- **Password Protection** — AES-256 encryption/decryption
- **HTML Export** — Convert presentations to HTML
- **Kotlin DSL** — Idiomatic Kotlin API with coroutines support
- **CJK Support** — Full Japanese/Chinese/Korean text round-trip

## Quick Start

```kotlin
// Create a presentation
val pres = Presentation()

val slide = pres.addSlide()
slide.addTextBox(914400, 2000000, 7315200, 1500000, "Hello DroidSlide!")

// Save
context.openFileOutput("demo.pptx", Context.MODE_PRIVATE).use { pres.save(it) }

// Open
val loaded = context.openFileInput("demo.pptx").use { Presentation.open(it) }
```

## Styled Text

```kotlin
val tb = slide.addTextBox(x, y, width, height)
val para = tb.addParagraph("Title", ParagraphStyle(alignment = TextAlignment.CENTER))
para.runs[0].style = TextStyle(bold = true, fontSize = 36.0, color = OfficeColor.Rgb(68, 114, 196))

tb.addParagraph().apply {
    addRun("Bold", TextStyle(bold = true))
    addRun(" and ", TextStyle())
    addRun("Italic", TextStyle(italic = true, fontName = "Arial"))
}
```

## Shapes & Tables

```kotlin
// Preset shape with fill
val shape = slide.addPresetShape(x, y, w, h, PresetShapeType.ROUND_RECT)
shape.shapeStyle = ShapeStyle(fill = Fill.Solid(OfficeColor.Rgb(68, 114, 196)))

// Table
val table = slide.addTable(x, y, w, h, rows = 3, cols = 4)
table.setCell(0, 0, "Header")

// Image
slide.addPicture(x, y, w, h, imageBytes, ImageFormat.PNG)
```

## Async API

```kotlin
val pres = Presentation.openAsync(inputStream)
pres.saveAsync(outputStream)
```

## Password Protection

```kotlin
pres.save(outputStream, password = "secret")
val loaded = Presentation.open(inputStream, password = "secret")
```

## HTML Export

```kotlin
val html = HtmlConverter.convert(presentation, title = "My Presentation")
```

## Installation

Add JitPack repository and dependency:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}

// build.gradle.kts
dependencies {
    implementation("com.droidoffice:droidslide:0.1.0-SNAPSHOT")
}
```

## Requirements

- Android API 26+ (Android 8.0)
- Kotlin 2.0+

## License

[Business Source License 1.1](LICENSE) — Free for personal, open source, non-profit, and educational use. Commercial license available via [Gumroad](https://y1uda.gumroad.com/).

## Comparison with Aspose.Slides

| Feature | DroidSlide | Aspose.Slides |
|---|---|---|
| Price | $99/year | $1,175+/year |
| Language | Kotlin-only | Java |
| Min SDK | API 26 | API 21 |
| File size | ~200KB | ~40MB |
| .pptx read/write | Yes | Yes |
| Shapes & Images | Yes | Yes |
| Charts | Basic | Advanced |
| Password | AES-256 | AES-256 |

---

Part of the [DroidOffice](https://droidoffice.com) suite: **DroidXLS** (Excel) · **DroidSlide** (PowerPoint) · **DroidDoc** (Word)
