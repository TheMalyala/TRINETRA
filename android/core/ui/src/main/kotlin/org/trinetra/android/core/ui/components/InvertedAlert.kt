package org.trinetra.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

@Composable
fun InvertedAlert(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.inverseBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title.uppercase(),
                color = colors.inverseInk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = message,
                color = colors.inverseInk,
                fontSize = 13.sp
            )
        }
    }
}
