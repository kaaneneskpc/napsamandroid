package com.kaaneneskpc.napsam.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Stitch son tasarımındaki çizgi ikonların birebir karşılığı (`docs/design/mobile-*.html`).
 *
 * SVG yayları, ayırıcısız bayrak yazımı ("0 11-18 0") ayrıştırıcıda sorun çıkarmasın diye ayrık
 * yazılmıştır. Renk `Icon(tint = …)` ile verilir; buradaki siyah yalnızca maske içindir.
 */
object NapsamIcons {
    val Bolt: ImageVector by lazy { icon("Bolt") { fill("M13 2L3 14h9l-1 8 10-12h-9l1-8z") } }

    val Check: ImageVector by lazy { icon("Check") { stroke("M5 13l4 4L19 7", width = 2.5f) } }

    val Refresh: ImageVector by lazy {
        icon("Refresh") {
            stroke(
                "M4 4v5h.582m15.356 2A8.001 8.001 0 0 0 4.582 9m0 0H9m11 11v-5h-.581m0 0" +
                    "a8.003 8.003 0 0 1-15.357-2m15.357 2H15",
                width = 2f,
            )
        }
    }

    val Heart: ImageVector by lazy { icon("Heart") { stroke(HEART_PATH, width = 1.8f) } }

    val HeartFilled: ImageVector by lazy {
        icon("HeartFilled") {
            fill(HEART_PATH)
            stroke(HEART_PATH, width = 1.8f)
        }
    }

    val Swap: ImageVector by lazy {
        icon("Swap") { stroke("M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4", width = 2f) }
    }

    val Clock: ImageVector by lazy {
        icon("Clock") { stroke("M12 8v4l3 3m6-3a9 9 0 1 1-18 0 9 9 0 0 1 18 0z", width = 2f) }
    }

    val Pin: ImageVector by lazy {
        icon("Pin") {
            stroke("M17.657 16.657L13.414 20.9a1.998 1.998 0 0 1-2.827 0l-4.244-4.243a8 8 0 1 1 11.314 0z", width = 2f)
            stroke("M15 11a3 3 0 1 1-6 0 3 3 0 0 1 6 0z", width = 2f)
        }
    }

    val Person: ImageVector by lazy {
        icon("Person") {
            stroke("M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM12 14a7 7 0 0 0-7 7h14a7 7 0 0 0-7-7z", width = 2f)
        }
    }

    val ChevronDown: ImageVector by lazy { icon("ChevronDown") { stroke("M19 9l-7 7-7-7", width = 2f) } }

    val Sun: ImageVector by lazy {
        icon("Sun") {
            stroke(
                "M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707" +
                    "M6.343 17.657l-.707.707M16 12a4 4 0 1 1-8 0 4 4 0 0 1 8 0z",
                width = 2f,
            )
        }
    }

    val Moon: ImageVector by lazy {
        icon("Moon", viewport = 20f) {
            fill("M17.293 13.293A8 8 0 0 1 6.707 2.707a8.001 8.001 0 1 0 10.586 10.586z")
        }
    }

    val Close: ImageVector by lazy { icon("Close") { stroke("M6 18L18 6M6 6l12 12", width = 2f) } }

    val Edit: ImageVector by lazy {
        icon("Edit") {
            stroke(
                "M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 1 1 3.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z",
                width = 2f,
            )
        }
    }

    val Back: ImageVector by lazy { icon("Back") { stroke("M10 19l-7-7m0 0l7-7m-7 7h18", width = 2f) } }
}

private const val HEART_PATH =
    "M4.318 6.318a4.5 4.5 0 0 0 0 6.364L12 20.364l7.682-7.682a4.5 4.5 0 0 0-6.364-6.364L12 7.636" +
        "l-1.318-1.318a4.5 4.5 0 0 0-6.364 0z"

private const val DEFAULT_VIEWPORT = 24f

private fun icon(
    name: String,
    viewport: Float = DEFAULT_VIEWPORT,
    block: ImageVector.Builder.() -> Unit,
): ImageVector = ImageVector.Builder(
    name = "Napsam.$name",
    defaultWidth = DEFAULT_VIEWPORT.dp,
    defaultHeight = DEFAULT_VIEWPORT.dp,
    viewportWidth = viewport,
    viewportHeight = viewport,
).apply(block).build()

private val Mask = SolidColor(Color.Black)

private fun ImageVector.Builder.fill(pathData: String) {
    addPath(pathData = addPathNodes(pathData), fill = Mask)
}

private fun ImageVector.Builder.stroke(pathData: String, width: Float) {
    addPath(
        pathData = addPathNodes(pathData),
        fill = null,
        stroke = Mask,
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}
