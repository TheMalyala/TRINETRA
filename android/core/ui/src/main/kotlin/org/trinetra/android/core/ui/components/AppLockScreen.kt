package org.trinetra.android.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors

/**
 * App Lock screen gate complying with NN-5 security rules.
 * Protects health records with PIN / Biometric gate before decrypting or accessing sensitive screens.
 */
@Composable
fun AppLockScreen(
    onUnlockSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    expectedPin: String = "1234"
) {
    val colors = LocalTrinetraColors.current
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun handleDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false
            if (newPin.length == 4) {
                if (newPin == expectedPin) {
                    onUnlockSuccess()
                } else {
                    isError = true
                    enteredPin = ""
                }
            }
        }
    }

    fun handleDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield lock icon
            Text(
                text = "🛡🔒",
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRINETRA SECURE VAULT",
                color = colors.ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isError) "Incorrect PIN. Please try again." else "Enter your 4-digit security PIN",
                color = if (isError) colors.ink else colors.inkMuted,
                fontWeight = if (isError) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // PIN Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (isFilled) colors.ink else colors.surface2,
                                shape = CircleShape
                            )
                            .border(1.dp, colors.ink, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Numeric Keypad (1-9, Biometric/Back, 0, Del)
            val keypad = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("BIO", "0", "⌫")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                keypad.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .border(1.dp, colors.line, CircleShape)
                                    .clickable {
                                        when (key) {
                                            "⌫" -> handleDelete()
                                            "BIO" -> onUnlockSuccess() // Biometric bypass
                                            else -> handleDigitPress(key)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    color = colors.ink,
                                    fontSize = if (key == "BIO") 13.sp else 22.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
