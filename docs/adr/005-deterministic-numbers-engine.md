# ADR 005: Deterministic Computation Engine for Numbers and Trends

## Status
Accepted

## Context
Large Language Models frequently hallucinate mathematical computations, unit conversions, and trend evaluations. In healthcare, an inverted trend or incorrect reference interval can be clinically catastrophic.

## Decision
All computations—including biomarker trend classifications (improving, stable, worsening), reference band comparisons, unit conversions (UCUM), medication adherence percentages, and bill spending aggregates—must be computed in pure Kotlin/Python code. The LLM is restricted to narrating pre-computed, verified structured outputs.

## Consequences
- **Positive**: 100% deterministic, reproducible, unit-testable clinical metrics (Non-Negotiable NN-2).
- **Negative**: Requires implementing and maintaining custom clinical analytics rules.
