package org.trinetra.android.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import org.trinetra.android.core.model.DoctorVerificationStatus

/**
 * Doctor verification badge complying with NN-12 and strict monochrome design rules.
 * Distinctly conveys status via glyphs and text labels, never color.
 */
@Composable
fun DoctorVerificationBadge(
    status: DoctorVerificationStatus,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current

    val (glyph, label, isBold) = when (status) {
        DoctorVerificationStatus.VERIFIED -> Triple("🛡✓", "Verified", true)
        DoctorVerificationStatus.UNVERIFIED -> Triple("🛡?", "Unverified", false)
        DoctorVerificationStatus.PENDING -> Triple("🛡⏳", "Pending Verification", false)
        DoctorVerificationStatus.REJECTED -> Triple("🛡✕", "Rejected", false)
    }

    Box(
        modifier = modifier
            .border(
                width = if (isBold) 1.5.dp else 1.dp,
                color = if (isBold) colors.ink else colors.line,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = glyph,
                color = colors.ink,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = if (isBold) colors.ink else colors.inkMuted,
                fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp,
                fontFamily = FontFamily.Default
            )
        }
    }
}
