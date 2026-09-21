package org.trinetra.android.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

@Composable
fun SourceChip(
    sourceId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current
    Box(
        modifier = modifier
            .border(1.dp, colors.line, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "[$sourceId]",
            color = colors.inkMuted,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
