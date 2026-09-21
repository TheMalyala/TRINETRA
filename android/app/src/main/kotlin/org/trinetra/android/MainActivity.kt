package org.trinetra.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.designsystem.theme.TrinetraTheme
import org.trinetra.android.core.model.AdviceEntry
import org.trinetra.android.core.model.AdviceOrigin
import org.trinetra.android.core.model.DoctorVerificationStatus
import org.trinetra.android.core.ui.components.AdviceCard
import org.trinetra.android.core.ui.components.AppLockScreen
import org.trinetra.android.core.ui.components.DoctorVerificationBadge
import org.trinetra.android.core.ui.components.ObservationValueText
import org.trinetra.android.core.ui.components.SourceChip
import org.trinetra.android.core.ui.components.TrinetraButton
import org.trinetra.android.core.ui.components.TrinetraOutlinedButton
import org.trinetra.android.core.ui.components.UrgentEmergencyBanner
import org.trinetra.android.core.ui.navigation.TrinetraDestination
import org.trinetra.android.core.ui.navigation.TrinetraNavigationBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrinetraTheme {
                TrinetraMainShell()
            }
        }
    }
}

@Composable
fun TrinetraMainShell() {
    val colors = LocalTrinetraColors.current
    var isLocked by remember { mutableStateOf(false) }
    var currentDestination by remember { mutableStateOf(TrinetraDestination.HOME) }

    if (isLocked) {
        AppLockScreen(
            onUnlockSuccess = { isLocked = false }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TrinetraTopBar(
                    activeProfile = stringResource(R.string.active_profile),
                    onLockClicked = { isLocked = true }
                )
            },
            bottomBar = {
                TrinetraNavigationBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = { currentDestination = it }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    TrinetraDestination.HOME -> HomeDashboardContent(
                        onNavigateToVault = { currentDestination = TrinetraDestination.VAULT },
                        onNavigateToCare = { currentDestination = TrinetraDestination.CARE }
                    )
                    TrinetraDestination.VAULT -> VaultTabContent()
                    TrinetraDestination.ASK -> DecoderTabContent()
                    TrinetraDestination.INBOX -> ConsultationsTabContent()
                    TrinetraDestination.CARE -> CareCircleTabContent()
                }
            }
        }
    }
}

@Composable
fun TrinetraTopBar(
    activeProfile: String,
    onLockClicked: () -> Unit
) {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    color = colors.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = activeProfile,
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                    .clickable(onClick = onLockClicked)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "🔒 " + stringResource(R.string.lock_vault),
                    color = colors.ink,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.line)
        )
    }
}

@Composable
fun HomeDashboardContent(
    onNavigateToVault: () -> Unit,
    onNavigateToCare: () -> Unit
) {
    val colors = LocalTrinetraColors.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Emergency Safety Net Banner (NN-11)
        UrgentEmergencyBanner()

        // Welcome / Tagline Section
        Column {
            Text(
                text = stringResource(R.string.tagline),
                color = colors.inkMuted,
                fontSize = 14.sp
            )
        }

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TrinetraButton(
                text = "+ Scan Document",
                onClick = onNavigateToVault,
                modifier = Modifier.weight(1f)
            )
            TrinetraOutlinedButton(
                text = "Advice Ledger",
                onClick = onNavigateToCare,
                modifier = Modifier.weight(1f)
            )
        }

        // Recent Biomarker Observation (Deterministic, Monochrome Out-of-Range Semantics)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "LATEST OBSERVATION",
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                ObservationValueText(
                    value = 7.4,
                    unit = "%",
                    refLow = 4.0,
                    refHigh = 5.6,
                    label = "Glycated Hemoglobin (HbA1c)"
                )
            }
        }

        // Recent Doctor Advice Entry
        val sampleAdvice = AdviceEntry(
            id = "adv-001",
            profileId = "prof-01",
            doctorId = "doc-01",
            body = "Continue Metformin 500mg once daily after meals. Maintain low glycemic index diet. Repeat HbA1c and lipid profile in 90 days.",
            tags = listOf("diabetes", "medication", "diet"),
            followUpOn = "2026-12-15",
            testsOrdered = listOf("HbA1c", "Lipid Profile"),
            origin = AdviceOrigin.DOCTOR_SIGNED,
            signature = "ecdsa_sig_valid",
            prevHash = "0".repeat(64),
            hash = "8f3b20c9e61284d79e8a5b23d90214ebc9103e67bfd9a102458ce3159042b10a"
        )
        AdviceCard(
            entry = sampleAdvice,
            doctorName = "Rajesh Sharma, MD (Diabetology)"
        )
    }
}

@Composable
fun VaultTabContent() {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.records_vault_title),
            color = colors.ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Encrypted local SQLite vault with FTS5 search indexing.",
            color = colors.inkMuted,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search bar placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "🔍 Search records by doctor, lab test, or date...",
                color = colors.inkMuted,
                fontSize = 14.sp
            )
        }

        // Category filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL (3)", "LAB (1)", "PRESCRIPTION (1)", "BILLS (1)").forEach { filter ->
                Box(
                    modifier = Modifier
                        .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = filter,
                        color = colors.ink,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun DecoderTabContent() {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.ask_netra_title),
            color = colors.ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Grounded clinical term explanations with citations. AI never diagnoses or prescribes.",
            color = colors.inkMuted,
            fontSize = 13.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DECODED: HbA1c",
                        color = colors.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    SourceChip(
                        sourceId = "MedlinePlus:LOINC 4548-4",
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "A test measuring average blood glucose over 2 to 3 months by examining glucose attached to hemoglobin.",
                    color = colors.ink,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "What to ask your doctor:",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "• Does my result meet the management target for my current regimen?",
                    color = colors.ink,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ConsultationsTabContent() {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.inbox_title),
            color = colors.ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Time-boxed, consent-scoped doctor messaging. Advice provided can be promoted directly to your Advice Ledger.",
            color = colors.inkMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
fun CareCircleTabContent() {
    val colors = LocalTrinetraColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.care_circle_title),
            color = colors.ink,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        // Doctor Card 1: Verified
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dr. Rajesh Sharma",
                        color = colors.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    DoctorVerificationBadge(status = DoctorVerificationStatus.VERIFIED)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Diabetology · Apollo Clinic · Reg #MCI-29481",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Doctor Card 2: Unverified (NN-12)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dr. Sunita Verma",
                        color = colors.ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    DoctorVerificationBadge(status = DoctorVerificationStatus.UNVERIFIED)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "General Physician · Pending council vetting",
                    color = colors.inkMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
