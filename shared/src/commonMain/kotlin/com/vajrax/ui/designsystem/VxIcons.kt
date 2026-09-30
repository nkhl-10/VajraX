package com.vajrax.ui.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Line icons (Lucide geometry, ISC licence) as themeable vectors — one stroke style for the
 * whole app instead of emoji / unicode glyphs. Tint them with `Icon(tint = …)`.
 */
object VxIcons {

    private fun circle(cx: Float, cy: Float, r: Float) =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0"

    private fun rect(x: Float, y: Float, w: Float, h: Float, rx: Float): String {
        val iw = w - 2 * rx
        val ih = h - 2 * rx
        return "M${x + rx} ${y}h${iw}a$rx $rx 0 0 1 $rx ${rx}v${ih}a$rx $rx 0 0 1 ${-rx} ${rx}h${-iw}" +
            "a$rx $rx 0 0 1 ${-rx} ${-rx}v${-ih}a$rx $rx 0 0 1 $rx ${-rx}z"
    }

    private fun icon(name: String, vararg paths: String): ImageVector = build(name, mirror = false, paths)

    /** Directional icons flip in right-to-left layouts (Arabic, Hebrew, Urdu…). */
    private fun directional(name: String, vararg paths: String): ImageVector = build(name, mirror = true, paths)

    private fun build(name: String, mirror: Boolean, paths: Array<out String>): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
            autoMirror = mirror
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = PathParser().parsePathString(d).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                )
            }
        }.build()

    val Home by lazy {
        icon(
            "home",
            "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
            "M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
        )
    }
    val Calendar by lazy { icon("calendar", "M8 2v4", "M16 2v4", rect(3f, 4f, 18f, 18f, 2f), "M3 10h18") }
    val Compass by lazy {
        icon(
            "compass", circle(12f, 12f, 10f),
            "m16.24 7.76-1.804 5.411a2 2 0 0 1-1.265 1.265L7.76 16.24l1.804-5.411a2 2 0 0 1 1.265-1.265z"
        )
    }
    val Chart by lazy { icon("chart", "M5 21v-6", "M12 21V3", "M19 21V9") }
    val User by lazy { icon("user", "M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2", circle(12f, 7f, 4f)) }
    val Check by lazy { icon("check", "M20 6 9 17l-5-5") }
    val Plus by lazy { icon("plus", "M5 12h14", "M12 5v14") }
    val Minus by lazy { icon("minus", "M5 12h14") }
    val Search by lazy { icon("search", circle(11f, 11f, 8f), "m21 21-4.3-4.3") }
    val Clock by lazy { icon("clock", circle(12f, 12f, 10f), "M12 6v6l4 2") }
    val ListChecks by lazy { icon("list-checks", "m3 17 2 2 4-4", "m3 7 2 2 4-4", "M13 6h8", "M13 12h8", "M13 18h8") }
    val Pencil by lazy {
        icon(
            "pencil",
            "M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z",
            "m15 5 4 4"
        )
    }
    val Flame by lazy {
        icon(
            "flame",
            "M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"
        )
    }
    val Award by lazy {
        icon(
            "award",
            "m15.477 12.89 1.515 8.526a.5.5 0 0 1-.81.47l-3.58-2.687a1 1 0 0 0-1.197 0l-3.586 2.686a.5.5 0 0 1-.81-.469l1.514-8.526",
            circle(12f, 8f, 6f)
        )
    }
    val Eye by lazy {
        icon(
            "eye",
            "M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0",
            circle(12f, 12f, 3f)
        )
    }
    val Grip by lazy {
        icon(
            "grip", circle(9f, 12f, 1f), circle(9f, 5f, 1f), circle(9f, 19f, 1f),
            circle(15f, 12f, 1f), circle(15f, 5f, 1f), circle(15f, 19f, 1f)
        )
    }
    val TextType by lazy { icon("type", "M4 7V4h16v3", "M9 20h6", "M12 4v16") }
    val Lightbulb by lazy {
        icon(
            "lightbulb",
            "M15 14c.2-1 .7-1.7 1.5-2.5 1-.9 1.5-2.2 1.5-3.5A6 6 0 0 0 6 8c0 1 .2 2.2 1.5 3.5.7.7 1.3 1.5 1.5 2.5",
            "M9 18h6", "M10 22h4"
        )
    }
    val Smartphone by lazy { icon("smartphone", rect(5f, 2f, 14f, 20f, 2f), "M12 18h.01") }
    val ArrowLeft by lazy { directional("arrow-left", "m12 19-7-7 7-7", "M19 12H5") }
    val ArrowRight by lazy { directional("arrow-right", "M5 12h14", "m12 5 7 7-7 7") }
    val ChevronRight by lazy { directional("chevron-right", "m9 18 6-6-6-6") }
    val ChevronLeft by lazy { directional("chevron-left", "m15 18-6-6 6-6") }
    val ChevronDown by lazy { icon("chevron-down", "m6 9 6 6 6-6") }
    val Close by lazy { icon("x", "M18 6 6 18", "m6 6 12 12") }
    val Bell by lazy {
        icon(
            "bell",
            "M10.268 21a2 2 0 0 0 3.464 0",
            "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326"
        )
    }
    val Moon by lazy { icon("moon", "M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z") }
    val Sun by lazy {
        icon(
            "sun", circle(12f, 12f, 4f), "M12 2v2", "M12 20v2", "m4.93 4.93 1.41 1.41", "m17.66 17.66 1.41 1.41",
            "M2 12h2", "M20 12h2", "m6.34 17.66-1.41 1.41", "m19.07 4.93-1.41 1.41"
        )
    }
    val Trash by lazy {
        icon("trash", "M3 6h18", "M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6", "M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2")
    }
    val Download by lazy { icon("download", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", "m7 10 5 5 5-5", "M12 15V3") }
    val Briefcase by lazy { icon("briefcase", "M16 20V4a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16", rect(2f, 6f, 20f, 14f, 2f)) }
    val MapPin by lazy {
        icon(
            "map-pin",
            "M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0",
            circle(12f, 10f, 3f)
        )
    }
    val Book by lazy {
        icon(
            "book-open", "M12 7v14",
            "M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z"
        )
    }
    val Zap by lazy {
        icon(
            "zap",
            "M4 14a1 1 0 0 1-.78-1.63l9.9-10.2a.5.5 0 0 1 .86.46l-1.92 6.02A1 1 0 0 0 13 10h7a1 1 0 0 1 .78 1.63l-9.9 10.2a.5.5 0 0 1-.86-.46l1.92-6.02A1 1 0 0 0 11 14z"
        )
    }
    val Droplet by lazy {
        icon("droplet", "M12 22a7 7 0 0 0 7-7c0-2-1-3.9-3-5.5s-3.5-4-4-6.5c-.5 2.5-2 4.9-4 6.5C6 11.1 5 13 5 15a7 7 0 0 0 7 7z")
    }
    val Dumbbell by lazy {
        icon(
            "dumbbell",
            "M17.596 12.768a2 2 0 1 0 2.829-2.829l-1.768-1.767a2 2 0 0 0 2.828-2.829l-2.828-2.828a2 2 0 0 0-2.829 2.828l-1.767-1.768a2 2 0 1 0-2.829 2.829z",
            "m2.5 21.5 1.4-1.4", "m20.1 3.9 1.4-1.4",
            "M5.343 21.485a2 2 0 1 0 2.829-2.828l1.767 1.768a2 2 0 1 0 2.829-2.829l-6.364-6.364a2 2 0 1 0-2.829 2.829l1.768 1.767a2 2 0 0 0-2.828 2.829z",
            "m9.6 14.4 4.8-4.8"
        )
    }
    val Activity by lazy {
        icon(
            "activity",
            "M22 12h-2.48a2 2 0 0 0-1.93 1.46l-2.35 8.36a.25.25 0 0 1-.48 0L9.24 2.18a.25.25 0 0 0-.48 0l-2.35 8.36A2 2 0 0 1 4.49 12H2"
        )
    }
    val Utensils by lazy {
        icon("utensils", "M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2", "M7 2v20", "M21 15V2a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7")
    }
    val Leaf by lazy {
        icon(
            "leaf",
            "M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z",
            "M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12"
        )
    }
    val Pen by lazy {
        icon(
            "square-pen",
            "M12 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7",
            "M18.375 2.625a1 1 0 0 1 3 3l-9.013 9.014a2 2 0 0 1-.853.505l-2.873.84a.5.5 0 0 1-.62-.62l.84-2.873a2 2 0 0 1 .506-.852z"
        )
    }
    val Wallet by lazy {
        icon(
            "wallet",
            "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1",
            "M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4"
        )
    }
    val Heart by lazy {
        icon(
            "heart",
            "M2 9.5a5.5 5.5 0 0 1 9.591-3.676.56.56 0 0 0 .818 0A5.49 5.49 0 0 1 22 9.5c0 2.29-1.5 4-3 5.5l-5.492 5.313a2 2 0 0 1-3 .019L5 15c-1.5-1.5-3-3.2-3-5.5"
        )
    }
    val Target by lazy { icon("target", circle(12f, 12f, 10f), circle(12f, 12f, 6f), circle(12f, 12f, 2f)) }
    val Sparkles by lazy {
        icon(
            "sparkles",
            "M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z",
            "M20 3v4", "M22 5h-4", "M4 17v2", "M5 18H3"
        )
    }
    val Sunrise by lazy {
        icon(
            "sunrise", "M12 2v8", "m4.93 10.93 1.41 1.41", "M2 18h2", "M20 18h2",
            "m19.07 10.93-1.41 1.41", "M22 22H2", "m8 6 4-4 4 4", "M16 18a4 4 0 0 0-8 0"
        )
    }
    val Users by lazy {
        icon(
            "users", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2", circle(9f, 7f, 4f),
            "M22 21v-2a4 4 0 0 0-3-3.87", "M16 3.13a4 4 0 0 1 0 7.75"
        )
    }
    val SkipForward by lazy { icon("skip-forward", "M5 4l10 8-10 8z", "M19 5v14") }
    val Alarm by lazy {
        icon(
            "alarm-clock", circle(12f, 13f, 8f), "M12 9v4l2 2", "M5 3 2 6", "m22 6-3-3",
            "M6.38 18.7 4 21", "M17.64 18.67 20 21"
        )
    }
    val Undo by lazy { icon("rotate-ccw", "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5") }
    val Play by lazy { icon("play", "M6 3l14 9-14 9z") }
    val Timer by lazy { icon("timer", "M10 2h4", "M12 14l3-3", circle(12f, 14f, 8f)) }
    val More by lazy { icon("more", circle(12f, 12f, 1f), circle(12f, 5f, 1f), circle(12f, 19f, 1f)) }
    val Globe by lazy {
        icon("globe", circle(12f, 12f, 10f), "M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20", "M2 12h20")
    }
    val Shield by lazy {
        icon(
            "shield",
            "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"
        )
    }
    val Info by lazy { icon("info", circle(12f, 12f, 10f), "M12 16v-4", "M12 8h.01") }
    val Archive by lazy { icon("archive", rect(2f, 3f, 20f, 5f, 1f), "M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8", "M10 12h4") }
    val Copy by lazy { icon("copy", rect(8f, 8f, 14f, 14f, 2f), "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2") }
    val TrendingUp by lazy { icon("trending-up", "M16 7h6v6", "m22 7-8.5 8.5-5-5L2 17") }
    val TrendingDown by lazy { icon("trending-down", "M16 17h6v-6", "m22 17-8.5-8.5-5 5L2 7") }
    val Note by lazy {
        icon(
            "file-text", "M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z",
            "M14 2v4a2 2 0 0 0 2 2h4", "M10 9H8", "M16 13H8", "M16 17H8"
        )
    }
    val Hash by lazy { icon("hash", "M4 9h16", "M4 15h16", "M10 3 8 21", "M16 3l-2 18") }
    val Gauge by lazy { icon("gauge", "m12 14 4-4", "M3.34 19a10 10 0 1 1 17.32 0") }
    val Repeat by lazy {
        icon("repeat", "m17 2 4 4-4 4", "M3 11v-1a4 4 0 0 1 4-4h14", "m7 22-4-4 4-4", "M21 13v1a4 4 0 0 1-4 4H3")
    }

    /** Icon for a habit's stored icon key (see HabitIconResolver.icons). */
    fun forKey(key: String?): ImageVector = when (key) {
        "sunrise" -> Sunrise
        "droplet" -> Droplet
        "leaf" -> Leaf
        "dumbbell" -> Dumbbell
        "activity" -> Activity
        "utensils" -> Utensils
        "book" -> Book
        "briefcase" -> Briefcase
        "pen" -> Pen
        "moon" -> Moon
        "clock" -> Clock
        "smartphone" -> Smartphone
        "wallet" -> Wallet
        "home" -> Home
        "heart" -> Heart
        "zap" -> Zap
        "mapPin" -> MapPin
        "target" -> Target
        "sparkles" -> Sparkles
        "sun" -> Sun
        "users" -> Users
        else -> Check
    }
}
