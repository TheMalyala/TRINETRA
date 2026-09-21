package org.trinetra.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

/**
 * Emergency notification banner complying with NN-11 and NMC Telemedicine guidelines.
 * Displayed with high contrast inverted tokens and direct emergency helpline actions.
 */
@Composable
fun UrgentEmergencyBanner(
    modifier: Modifier = Modifier,
    onDialEmergency: ((String) -> Unit)? = null
) {
    val colors = LocalTrinetraColors.current
    val bannerShape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.inverseBackground, bannerShape)
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✦ URGENT",
                    color = colors.inverseInk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Medical Emergency Notice",
                    color = colors.inverseInk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Trinetra is an archival and explanatory tool, not an emergency service. If experiencing chest pain, severe breathlessness, or collapse, call emergency services immediately.",
                color = colors.inverseInk,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .border(1.dp, colors.inverseInk, RoundedCornerShape(4.dp))
                        .clickable { onDialEmergency?.invoke("112") }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DIAL 112 (National)",
                        color = colors.inverseInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .border(1.dp, colors.inverseInk, RoundedCornerShape(4.dp))
                        .clickable { onDialEmergency?.invoke("108") }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "DIAL 108 (Ambulance)",
                        color = colors.inverseInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
