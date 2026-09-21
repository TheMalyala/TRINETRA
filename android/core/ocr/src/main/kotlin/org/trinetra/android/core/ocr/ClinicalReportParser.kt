package org.trinetra.android.core.ocr

import java.util.UUID

data class ExtractedObservationCandidate(
    val id: String = UUID.randomUUID().toString(),
    var nameAsPrinted: String,
    var loincCode: String,
    var valueNum: Double?,
    var valueText: String? = null,
    var unitUcum: String,
    var refLow: Double? = null,
    var refHigh: Double? = null,
    val sourceSnippet: String,
    var isConfirmed: Boolean = false
)

/**
 * Deterministic laboratory report OCR parser adhering to:
 * - NN-2: Numbers come from deterministic code, never an LLM.
 * - NN-3: Ingested facts map to registered LOINC codes from knowledge/SOURCES.md.
 * - NN-4: Candidate values preserve source snippets for user visual confirmation.
 */
object ClinicalReportParser {

    private data class BiomarkerRule(
        val pattern: Regex,
        val defaultName: String,
        val loincCode: String,
        val defaultUnit: String,
        val defaultRefLow: Double? = null,
        val defaultRefHigh: Double? = null
    )

    private val RULES = listOf(
        // Hemoglobin (LOINC 718-7, g/dL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:hemoglobin|hb)\b[:\s]*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
            defaultName = "Hemoglobin",
            loincCode = "718-7",
            defaultUnit = "g/dL",
            defaultRefLow = 13.0,
            defaultRefHigh = 17.0
        ),
        // Total Leucocyte Count / WBC (LOINC 6690-2, /uL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:total\s+leucocyte|tlc|wbc)\b[:\s]*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE),
            defaultName = "Total Leucocyte Count (TLC)",
            loincCode = "6690-2",
            defaultUnit = "/uL",
            defaultRefLow = 4000.0,
            defaultRefHigh = 11000.0
        ),
        // Platelet Count (LOINC 777-3, /uL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:platelet|platelets|plt)\b[:\s]*([0-9]+(?:\.[0-9]+)?)""", RegexOption.IGNORE_CASE),
            defaultName = "Platelet Count",
            loincCode = "777-3",
            defaultUnit = "/uL",
            defaultRefLow = 150000.0,
            defaultRefHigh = 450000.0
        ),
        // Glycated Hemoglobin / HbA1c (LOINC 4548-4, %)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:hba1c|glycosylated\s+hemoglobin)\b[:\s]*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
            defaultName = "HbA1c (Glycosylated Hemoglobin)",
            loincCode = "4548-4",
            defaultUnit = "%",
            defaultRefLow = 4.0,
            defaultRefHigh = 5.6
        ),
        // Fasting Blood Glucose / Sugar (LOINC 1558-6, mg/dL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:fasting\s+blood\s+sugar|fbs|fasting\s+glucose)\b[:\s]*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
            defaultName = "Fasting Blood Glucose",
            loincCode = "1558-6",
            defaultUnit = "mg/dL",
            defaultRefLow = 70.0,
            defaultRefHigh = 99.0
        ),
        // Serum Creatinine (LOINC 2160-0, mg/dL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:serum\s+creatinine|creatinine)\b[:\s]*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
            defaultName = "Serum Creatinine",
            loincCode = "2160-0",
            defaultUnit = "mg/dL",
            defaultRefLow = 0.7,
            defaultRefHigh = 1.3
        ),
        // Total Cholesterol (LOINC 2093-3, mg/dL)
        BiomarkerRule(
            pattern = Regex("""(?i)\b(?:total\s+cholesterol|cholesterol)\b[:\s]*([0-9]+\.?[0-9]*)""", RegexOption.IGNORE_CASE),
            defaultName = "Total Cholesterol",
            loincCode = "2093-3",
            defaultUnit = "mg/dL",
            defaultRefLow = null,
            defaultRefHigh = 200.0
        )
    )

    private val RANGE_REGEX = Regex("""(?i)(?:ref|range|normal)?[:\s]*([0-9]+\.?[0-9]*)\s*[-–]\s*([0-9]+\.?[0-9]*)""")
    private val LESS_THAN_REGEX = Regex("""(?i)<\s*([0-9]+\.?[0-9]*)""")
    private val GREATER_THAN_REGEX = Regex("""(?i)>\s*([0-9]+\.?[0-9]*)""")

    /**
     * Parses raw OCR text lines and extracts clinical observation candidates with their source lines.
     */
    fun parse(ocrText: String): List<ExtractedObservationCandidate> {
        val lines = ocrText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val candidates = mutableListOf<ExtractedObservationCandidate>()
        val matchedLoincCodes = mutableSetOf<String>()

        for (line in lines) {
            for (rule in RULES) {
                if (matchedLoincCodes.contains(rule.loincCode)) continue

                val match = rule.pattern.find(line)
                if (match != null) {
                    val valueNum = match.groupValues[1].toDoubleOrNull()
                    if (valueNum != null) {
                        // Attempt to extract reference range from the same line
                        var refLow = rule.defaultRefLow
                        var refHigh = rule.defaultRefHigh

                        val rangeMatch = RANGE_REGEX.find(line)
                        if (rangeMatch != null) {
                            refLow = rangeMatch.groupValues[1].toDoubleOrNull() ?: refLow
                            refHigh = rangeMatch.groupValues[2].toDoubleOrNull() ?: refHigh
                        } else {
                            val ltMatch = LESS_THAN_REGEX.find(line)
                            if (ltMatch != null) {
                                refHigh = ltMatch.groupValues[1].toDoubleOrNull() ?: refHigh
                            }
                            val gtMatch = GREATER_THAN_REGEX.find(line)
                            if (gtMatch != null) {
                                refLow = gtMatch.groupValues[1].toDoubleOrNull() ?: refLow
                            }
                        }

                        candidates.add(
                            ExtractedObservationCandidate(
                                nameAsPrinted = rule.defaultName,
                                loincCode = rule.loincCode,
                                valueNum = valueNum,
                                unitUcum = rule.defaultUnit,
                                refLow = refLow,
                                refHigh = refHigh,
                                sourceSnippet = line,
                                isConfirmed = false // Crucial: NN-4 unconfirmed by default
                            )
                        )
                        matchedLoincCodes.add(rule.loincCode)
                        break
                    }
                }
            }
        }

        return candidates
    }
}
