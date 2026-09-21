package org.trinetra.android.core.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DecodedTerm(
    val term: String,
    @SerialName("one_liner")
    val oneLiner: String,
    @SerialName("plain_explanation")
    val plainExplanation: String,
    @SerialName("what_to_ask_doctor")
    val whatToAskDoctor: String,
    @SerialName("source_id")
    val sourceId: String
)

@Serializable
data class AICitation(
    @SerialName("source_id")
    val sourceId: String,
    @SerialName("source_name")
    val sourceName: String,
    val url: String? = null,
    @SerialName("reference_text")
    val referenceText: String? = null
)

@Serializable
data class AIQueryResponse(
    @SerialName("response_text")
    val responseText: String,
    @SerialName("is_emergency")
    val isEmergency: Boolean = false,
    @SerialName("emergency_numbers")
    val emergencyNumbers: List<String> = emptyList(),
    @SerialName("disclaimer_included")
    val disclaimerIncluded: Boolean = true,
    val intent: String = "STANDARD_QUERY",
    val citations: List<AICitation> = emptyList(),
    @SerialName("questions_for_doctor")
    val questionsForDoctor: List<String> = emptyList()
)

@Serializable
data class DrugProduct(
    @SerialName("brand_name")
    val brandName: String,
    @SerialName("generic_name")
    val genericName: String,
    @SerialName("therapeutic_class")
    val therapeuticClass: String,
    @SerialName("common_forms")
    val commonForms: List<String> = emptyList(),
    @SerialName("source_id")
    val sourceId: String
)
