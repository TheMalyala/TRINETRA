package org.trinetra.android.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Strict Monochrome Tokens
val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)

// Light Theme Tokens
val LightBg = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF6F6F6)
val LightSurface2 = Color(0xFFEDEDED)
val LightInk = Color(0xFF000000)
val LightInkMuted = Color(0xFF5C5C5C)
val LightLine = Color(0xFFD9D9D9)
val LightInverseBg = Color(0xFF000000)
val LightInverseInk = Color(0xFFFFFFFF)

// Dark Theme Tokens
val DarkBg = Color(0xFF000000)
val DarkSurface = Color(0xFF0D0D0D)
val DarkSurface2 = Color(0xFF1A1A1A)
val DarkInk = Color(0xFFFFFFFF)
val DarkInkMuted = Color(0xFFA3A3A3)
val DarkLine = Color(0xFF2E2E2E)
val DarkInverseBg = Color(0xFFFFFFFF)
val DarkInverseInk = Color(0xFF000000)

@Immutable
data class TrinetraColors(
    val background: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val inkMuted: Color,
    val line: Color,
    val inverseBackground: Color,
    val inverseInk: Color
)

val LocalTrinetraColors = staticCompositionLocalOf {
    TrinetraColors(
        background = LightBg,
        surface = LightSurface,
        surface2 = LightSurface2,
        ink = LightInk,
        inkMuted = LightInkMuted,
        line = LightLine,
        inverseBackground = LightInverseBg,
        inverseInk = LightInverseInk
    )
}
