package org.trinetra.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

/**
 * Primary high-contrast monochrome button.
 * Filled background with inverse text, complying with accessibility target size (>= 48dp).
 */
@Composable
fun TrinetraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalTrinetraColors.current
    val shape = RoundedCornerShape(8.dp)

    val bgColor = if (enabled) colors.ink else colors.surface2
    val textColor = if (enabled) colors.inverseInk else colors.inkMuted

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth()
            .background(bgColor, shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Secondary outlined monochrome button.
 * Outlined border with transparent surface.
 */
@Composable
fun TrinetraOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LocalTrinetraColors.current
    val shape = RoundedCornerShape(8.dp)

    val borderColor = if (enabled) colors.line else colors.surface2
    val textColor = if (enabled) colors.ink else colors.inkMuted

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth()
            .border(1.dp, borderColor, shape)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
