package org.trinetra.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColorScheme = lightColorScheme(
    primary = LightInk,
    onPrimary = LightInverseInk,
    background = LightBg,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    outline = LightLine
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInk,
    onPrimary = DarkInverseInk,
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    outline = DarkLine
)

@Composable
fun TrinetraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val trinetraColors = if (darkTheme) {
        TrinetraColors(
            background = DarkBg,
            surface = DarkSurface,
            surface2 = DarkSurface2,
            ink = DarkInk,
            inkMuted = DarkInkMuted,
            line = DarkLine,
            inverseBackground = DarkInverseBg,
            inverseInk = DarkInverseInk
        )
    } else {
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

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalTrinetraColors provides trinetraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}
