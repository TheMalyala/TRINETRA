package org.trinetra.android.core.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.trinetra.android.core.ai.model.AIQueryResponse
import org.trinetra.android.core.ai.model.DecodedTerm

interface NetraRepository {
    suspend fun decodeTerm(term: String): DecodedTerm
    suspend fun queryNetra(prompt: String): AIQueryResponse
}

class DefaultNetraRepository : NetraRepository {

    override suspend fun decodeTerm(term: String): DecodedTerm = withContext(Dispatchers.IO) {
        // Fast local-first lookup
        val local = LocalKnowledgeDictionary.findTerm(term)
        if (local != null) return@withContext local

        // Fallback for unseeded queries
        DecodedTerm(
            term = term,
            oneLiner = "Clinical parameter or medical term.",
            plainExplanation = "Medical terms and observations describe specific physiological indicators or diagnostic tests.",
            whatToAskDoctor = "How does this result relate to my overall health and diagnosis?",
            sourceId = "src_curated_glossary"
        )
    }

    override suspend fun queryNetra(prompt: String): AIQueryResponse = withContext(Dispatchers.IO) {
        LocalKnowledgeDictionary.evaluateOfflineQuery(prompt)
    }
}
