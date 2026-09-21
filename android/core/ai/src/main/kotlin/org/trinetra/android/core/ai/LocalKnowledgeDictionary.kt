package org.trinetra.android.core.ai

import org.trinetra.android.core.ai.model.AICitation
import org.trinetra.android.core.ai.model.AIQueryResponse
import org.trinetra.android.core.ai.model.DecodedTerm
import org.trinetra.android.core.ai.model.DrugProduct

/**
 * Embedded offline knowledge dictionary matching knowledge/SOURCES.md.
 * Enables zero-network local decoding on Android devices (NN-5).
 */
object LocalKnowledgeDictionary {

    val SEED_TERMS: Map<String, DecodedTerm> = listOf(
        DecodedTerm(
            term = "HbA1c",
            oneLiner = "A blood test showing average blood sugar levels over the past 2 to 3 months.",
            plainExplanation = "HbA1c measures the percentage of hemoglobin (the oxygen-carrying protein in red blood cells) coated with sugar. Because red blood cells live for about 3 months, it reflects average blood glucose over that timeframe.",
            whatToAskDoctor = "What is my personal target HbA1c range, and what changes to diet, exercise, or medications will help achieve it?",
            sourceId = "src_medlineplus"
        ),
        DecodedTerm(
            term = "Dyspnoea",
            oneLiner = "Shortness of breath or difficulty breathing.",
            plainExplanation = "Dyspnoea is the sensation of feeling winded, breathless, or having to work hard to breathe. It can occur temporarily after intense physical exertion or may be linked to lung, heart, or respiratory conditions.",
            whatToAskDoctor = "What tests or evaluations can help identify why I feel breathless, and are there warning signs I should watch out for?",
            sourceId = "src_medlineplus"
        ),
        DecodedTerm(
            term = "Idiopathic",
            oneLiner = "A condition that arises spontaneously or without an identifiable cause.",
            plainExplanation = "When doctors describe a symptom or illness as idiopathic, it means thorough medical testing has not identified a specific underlying disease or trigger.",
            whatToAskDoctor = "Are there other diagnostic options to consider, or will we focus on managing the symptoms?",
            sourceId = "src_medlineplus"
        ),
        DecodedTerm(
            term = "Creatinine",
            oneLiner = "A waste product from muscle breakdown filtered out by the kidneys.",
            plainExplanation = "Creatinine is produced at a steady rate by muscles and excreted entirely by healthy kidneys. Higher blood levels generally indicate reduced kidney filtering capability.",
            whatToAskDoctor = "What does my creatinine trend say about my overall kidney function?",
            sourceId = "src_medlineplus"
        ),
        DecodedTerm(
            term = "Hemoglobin",
            oneLiner = "An iron-rich protein inside red blood cells that transports oxygen.",
            plainExplanation = "Hemoglobin binds oxygen in the lungs and delivers it to cells throughout the body. Low levels define anemia, while elevated levels can point to dehydration or bone marrow disorders.",
            whatToAskDoctor = "If my hemoglobin is low, what dietary or iron supplement options should we explore?",
            sourceId = "src_medlineplus"
        )
    ).associateBy { it.term.lowercase() }

    val SEED_DRUGS: List<DrugProduct> = listOf(
        DrugProduct(
            brandName = "Augmentin",
            genericName = "Amoxicillin and Potassium Clavulanate",
            therapeuticClass = "Antibacterial",
            commonForms = listOf("Tablet", "Syrup"),
            sourceId = "src_openfda"
        ),
        DrugProduct(
            brandName = "Glycomet",
            genericName = "Metformin Hydrochloride",
            therapeuticClass = "Antidiabetic",
            commonForms = listOf("Tablet"),
            sourceId = "src_openfda"
        ),
        DrugProduct(
            brandName = "Pan 40",
            genericName = "Pantoprazole",
            therapeuticClass = "Proton Pump Inhibitor (Antacid)",
            commonForms = listOf("Tablet", "Injection"),
            sourceId = "src_openfda"
        ),
        DrugProduct(
            brandName = "Telma 40",
            genericName = "Telmisartan",
            therapeuticClass = "Angiotensin II Receptor Blocker (Antihypertensive)",
            commonForms = listOf("Tablet"),
            sourceId = "src_openfda"
        )
    )

    fun findTerm(query: String): DecodedTerm? {
        val clean = query.trim().lowercase()
        return SEED_TERMS[clean] ?: SEED_TERMS.values.find {
            it.term.lowercase().contains(clean) || clean.contains(it.term.lowercase())
        }
    }

    fun findDrug(query: String): DrugProduct? {
        val clean = query.trim().lowercase()
        return SEED_DRUGS.find {
            it.brandName.lowercase().contains(clean) || it.genericName.lowercase().contains(clean)
        }
    }

    fun evaluateOfflineQuery(prompt: String): AIQueryResponse {
        val lower = prompt.lowercase()

        // Red-flag emergency intercept (NN-11)
        if (lower.contains("chest pain") || lower.contains("cannot breathe") || lower.contains("heart attack") || lower.contains("stroke")) {
            return AIQueryResponse(
                responseText = "EMERGENCY: Symptoms indicate a potential medical emergency. Please call 112 or 108 immediately or proceed to the nearest emergency department.",
                isEmergency = true,
                emergencyNumbers = listOf("112", "108"),
                disclaimerIncluded = false,
                intent = "TRIGGER_EMERGENCY_INTERCEPT",
                citations = listOf(AICitation("src_curated_glossary", "National Emergency Protocols (112/108)"))
            )
        }

        // Diagnosis refusal check (NN-1)
        if (lower.contains("do i have") || lower.contains("am i suffering") || lower.contains("diagnose")) {
            return AIQueryResponse(
                responseText = "This is not a diagnosis. Trinetra does not provide diagnostic assessments. Please discuss with your doctor for comprehensive evaluation.",
                isEmergency = false,
                disclaimerIncluded = true,
                intent = "REFUSE_DIAGNOSIS",
                citations = listOf(AICitation("src_curated_glossary", "Trinetra Curated Indian Medical Glossary")),
                questionsForDoctor = listOf("What could be causing these specific test values?", "Are confirmatory tests recommended?")
            )
        }

        // Prescription refusal check (NN-1)
        if (lower.contains("should i take") || lower.contains("units of insulin") || lower.contains("increase dose")) {
            return AIQueryResponse(
                responseText = "Trinetra cannot prescribe medications or suggest dosage adjustments. Modifying medical regimens without clinical supervision is unsafe. Please contact your doctor promptly.",
                isEmergency = false,
                disclaimerIncluded = true,
                intent = "REFUSE_AND_URGE_CONSULTATION",
                citations = listOf(AICitation("src_curated_glossary", "Trinetra Curated Indian Medical Glossary")),
                questionsForDoctor = listOf("What is my target reading range?", "Should my dosage be adjusted by my physician?")
            )
        }

        // Term match
        val term = findTerm(prompt)
        if (term != null) {
            return AIQueryResponse(
                responseText = "${term.term}: ${term.oneLiner}\n\n${term.plainExplanation}",
                isEmergency = false,
                disclaimerIncluded = true,
                intent = "DECODE_TERM",
                citations = listOf(AICitation(term.sourceId, "MedlinePlus (U.S. National Library of Medicine)")),
                questionsForDoctor = listOf(term.whatToAskDoctor)
            )
        }

        // Drug match
        val drug = findDrug(prompt)
        if (drug != null) {
            return AIQueryResponse(
                responseText = "${drug.brandName} contains the active ingredient ${drug.genericName}. It belongs to the therapeutic class of ${drug.therapeuticClass}.",
                isEmergency = false,
                disclaimerIncluded = true,
                intent = "EXPLAIN_DRUG",
                citations = listOf(AICitation(drug.sourceId, "openFDA Drug Product Labels")),
                questionsForDoctor = listOf(
                    "Why was ${drug.brandName} chosen for my condition?",
                    "What precautions or food interactions should I follow?"
                )
            )
        }

        return AIQueryResponse(
            responseText = "Trinetra explains medical terms and lab tests. Try searching for HbA1c, Dyspnoea, Creatinine, or prescription brand names.",
            isEmergency = false,
            disclaimerIncluded = true,
            intent = "GENERAL_EDUCATIONAL",
            citations = listOf(AICitation("src_curated_glossary", "Trinetra Curated Indian Medical Glossary"))
        )
    }
}
