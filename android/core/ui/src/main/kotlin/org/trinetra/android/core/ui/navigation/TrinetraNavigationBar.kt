package org.trinetra.android.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

enum class TrinetraDestination(
    val label: String,
    val iconGlyph: String,
    val selectedGlyph: String
) {
    HOME("Home", "⌂", "▲"),
    VAULT("Vault", "□", "■"),
    ASK("Ask", "◇", "◆"),
    INBOX("Inbox", "✉", "✉!"),
    CARE("Care", "○", "●")
}

/**
 * Bottom navigation bar for Trinetra adhering strictly to monochrome design tokens.
 * Selected item is clearly indicated through glyph change, bold typography, and indicator line.
 */
@Composable
fun TrinetraNavigationBar(
    currentDestination: TrinetraDestination,
    onDestinationSelected: (TrinetraDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        // 1dp top divider line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrinetraDestination.entries.forEach { destination ->
                val isSelected = destination == currentDestination

                Column(
                    modifier = Modifier
                        .clickable(
                            role = Role.Tab,
                            onClick = { onDestinationSelected(destination) }
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSelected) destination.selectedGlyph else destination.iconGlyph,
                        color = if (isSelected) colors.ink else colors.inkMuted,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = destination.label,
                        color = if (isSelected) colors.ink else colors.inkMuted,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
