# AI Guardrails & RAG Pipeline

The AI system ("Netra") operates as an explanatory interface. It is architected with strict defensive safety barriers to prevent medical hallucinations or unauthorized diagnostic claims.

## Pipeline Architecture

```
User Query / Voice
        │
        ▼
[ Red-Flag Classifier ] ────(Emergency Detected)────► [ EMERGENCY UI (112 / 108) ]
        │ (Safe)
        ▼
[ Intent Router ]
  ├── explain-report
  ├── explain-drug
  ├── decode-term
  └── summarise-history
        │
        ▼
[ Context Retrieval ]
  ├── Patient Facts (Local Index / Observations)
  └── Grounded Medical Knowledge (pgvector Knowledge Store)
        │
        ▼
[ PHI Redaction Filter ] (Presidio strips real identifiers if calling remote models)
        │
        ▼
[ LLM Generation ] (Ollama Apache-2 model or configured adapter)
        │
        ▼
[ Structured Response Validator ]
  ├── Citation Verifier: Every fact cites valid obs_* or src_* IDs
  ├── Numerical Fidelity: Every number matches verified input values exactly
  └── Forbidden Phrase Filter: Rejects imperatives, diagnoses, and dosage changes
        │
        ▼
Rendered UI with Source Chips
```

## Grounding & Citations
- Non-Negotiable **NN-2**: Numbers and trends originate exclusively from deterministic Kotlin code.
- Non-Negotiable **NN-3**: Uncited clinical statements cause the response to be automatically rejected and routed to safe fallback text.
