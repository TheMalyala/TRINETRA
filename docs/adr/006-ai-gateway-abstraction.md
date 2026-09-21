# ADR 006: AI Gateway Abstraction with Pre-Inference PHI Redaction

## Status
Accepted

## Context
Deploying AI assistants in clinical contexts presents vendor lock-in risks, license constraints, and privacy exposure if sensitive PHI is transmitted to commercial third-party cloud APIs.

## Decision
We introduce an **AI Gateway** layer:
1. Unified adapter interface supporting Ollama (local Apache-2 models), OpenAI-compatible endpoints, and optional cloud providers.
2. Mandatory pre-inference redaction pipeline using Microsoft Presidio + custom Indian identifier patterns (Aadhaar, ABHA, phone numbers) before any prompt reaches a non-local LLM.
3. Strict JSON schema validation on all model responses, rejecting outputs missing citations or violating safety filters.

## Consequences
- **Positive**: Complies with NN-9 and NN-10; enables swapping model backends seamlessly without changing client or application logic.
- **Negative**: Adds minor serialization latency for token streaming and response validation.
