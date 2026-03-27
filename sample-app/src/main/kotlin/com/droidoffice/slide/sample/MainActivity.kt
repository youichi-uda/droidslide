package com.droidoffice.slide.sample

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvLog = findViewById(R.id.tvLog)

        findViewById<Button>(R.id.btnBasicReadWrite).setOnClickListener { runDemo { Demos.basicReadWrite(this) } }
        findViewById<Button>(R.id.btnStyledText).setOnClickListener { runDemo { Demos.styledText(this) } }
        findViewById<Button>(R.id.btnShapesImages).setOnClickListener { runDemo { Demos.shapesAndImages(this) } }
        findViewById<Button>(R.id.btnSlideOps).setOnClickListener { runDemo { Demos.slideOperations(this) } }
        findViewById<Button>(R.id.btnCharts).setOnClickListener { runDemo { Demos.charts(this) } }
        findViewById<Button>(R.id.btnPassword).setOnClickListener { runDemo { Demos.passwordProtection(this) } }
        findViewById<Button>(R.id.btnHtmlExport).setOnClickListener { runDemo { Demos.htmlExport(this) } }
        findViewById<Button>(R.id.btnFullPresentation).setOnClickListener { runDemo { Demos.fullPresentation(this) } }
        findViewById<Button>(R.id.btnRunAll).setOnClickListener { runAllDemos() }
    }

    private fun runDemo(block: suspend () -> String) {
        scope.launch {
            tvLog.text = "Running...\n"
            try {
                val result = withContext(Dispatchers.IO) { block() }
                tvLog.append(result + "\n")
            } catch (e: Exception) {
                tvLog.append("ERROR: ${e.message}\n")
            }
            tvLog.append("Done.\n")
        }
    }

    private fun runAllDemos() {
        scope.launch {
            tvLog.text = "Running all demos...\n"
            val demos = listOf(
                "Basic Read/Write" to suspend { Demos.basicReadWrite(this@MainActivity) },
                "Styled Text" to suspend { Demos.styledText(this@MainActivity) },
                "Shapes & Images" to suspend { Demos.shapesAndImages(this@MainActivity) },
                "Slide Operations" to suspend { Demos.slideOperations(this@MainActivity) },
                "Charts" to suspend { Demos.charts(this@MainActivity) },
                "Password Protection" to suspend { Demos.passwordProtection(this@MainActivity) },
                "HTML Export" to suspend { Demos.htmlExport(this@MainActivity) },
                "Full Presentation" to suspend { Demos.fullPresentation(this@MainActivity) },
            )
            for ((name, demo) in demos) {
                tvLog.append("--- $name ---\n")
                try {
                    val result = withContext(Dispatchers.IO) { demo() }
                    tvLog.append(result + "\n")
                } catch (e: Exception) {
                    tvLog.append("ERROR: ${e.message}\n")
                }
            }
            tvLog.append("All demos complete.\n")
        }
    }
}
