package org.trinetra.android.feature.decoder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.trinetra.android.core.ai.DefaultNetraRepository
import org.trinetra.android.core.ai.NetraRepository
import org.trinetra.android.core.ai.model.AIQueryResponse
import org.trinetra.android.core.designsystem.theme.LocalTrinetraColors
import org.trinetra.android.core.ui.components.SourceChip
import org.trinetra.android.core.ui.components.TrinetraPrimaryButton
import org.trinetra.android.core.ui.components.UrgentEmergencyBanner

@Composable
fun MedicalTermDecoderScreen(
    modifier: Modifier = Modifier,
    repository: NetraRepository = remember { DefaultNetraRepository() },
    onEmergencyCall: (String) -> Unit = {}
) {
    val colors = LocalTrinetraColors.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var queryResponse by remember { mutableStateOf<AIQueryResponse?>(null) }
    var activeSourceAttribution by remember { mutableStateOf<String?>(null) }

    val quickTerms = listOf("HbA1c", "Dyspnoea", "Idiopathic", "Augmentin", "Creatinine")

    fun executeQuery(query: String) {
        if (query.isBlank()) return
        coroutineScope.launch {
            queryResponse = repository.queryNetra(query)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "NETRA DECODER",
                    color = colors.ink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Translate medical jargon, lab parameters, and prescription drug names into plain language.",
                    color = colors.inkMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Search Input
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter term, biomarker, or drug...",
                            color = colors.inkMuted,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { executeQuery(searchQuery) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.ink,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick suggestion chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(quickTerms) { term ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, colors.line, RoundedCornerShape(16.dp))
                                    .clickable {
                                        searchQuery = term
                                        executeQuery(term)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = term,
                                    color = colors.ink,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TrinetraPrimaryButton(
                        text = "Decode",
                        onClick = { executeQuery(searchQuery) },
                        modifier = Modifier.height(36.dp)
                    )
                }
            }
        }

        // Emergency Intercept Banner (NN-11)
        if (queryResponse?.isEmergency == true) {
            item {
                UrgentEmergencyBanner(
                    onDialEmergency = onEmergencyCall
                )
            }
        }

        // Active Result Card
        queryResponse?.let { resp ->
            if (!resp.isEmergency) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                            .background(colors.surface, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Status / Intent Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (resp.intent.startsWith("REFUSE")) "SAFETY NOTICE" else "EXPLANATION",
                                    color = colors.ink,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    resp.citations.forEach { cite ->
                                        SourceChip(
                                            sourceId = cite.sourceId,
                                            onClick = {
                                                activeSourceAttribution = "${cite.sourceName} (${cite.sourceId})"
                                            }
                                        )
                                    }
                                }
                            }

                            // Body Prose
                            Text(
                                text = resp.responseText,
                                color = colors.ink,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )

                            // Questions for Doctor Section
                            if (resp.questionsForDoctor.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "QUESTIONS TO ASK YOUR DOCTOR:",
                                    color = colors.inkMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 0.5.sp
                                )

                                resp.questionsForDoctor.forEach { question ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "•", color = colors.ink, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = question,
                                            color = colors.ink,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Source Attribution Callout
        activeSourceAttribution?.let { attribution ->
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                        .background(colors.surface)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "CANONICAL SOURCE ATTRIBUTION",
                            color = colors.inkMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = attribution,
                            color = colors.ink,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Educational Disclaimer Footer (NN-1)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, colors.line, RoundedCornerShape(4.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "DISCLAIMER: Netra provides educational summaries and preparation questions only. It never diagnoses conditions, prescribes medications, or adjusts doses. Always consult your registered physician.",
                    color = colors.inkMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
