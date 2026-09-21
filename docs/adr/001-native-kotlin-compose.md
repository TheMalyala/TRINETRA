# ADR 001: Native Kotlin + Jetpack Compose Architecture

## Status
Accepted

## Context
Trinetra is an Android health-records app requiring deep access to platform security and hardware capabilities: Android Keystore, BiometricPrompt, CameraX, ML Kit on-device models, WorkManager exact alarms, and local speech APIs. Cross-platform frameworks (React Native, Flutter) introduce additional JNI/bridge overhead and lag behind native Android security features.

## Decision
We build Trinetra natively using Kotlin 2.x and Jetpack Compose with a multi-module Clean Architecture and Unidirectional Data Flow (UDF).

## Consequences
- **Positive**: Native access to Android Keystore, SQLCipher, ML Kit, and biometric primitives; superior UI fluidity and accessibility.
- **Negative**: Code is Android-only. If iOS is ever supported, domain logic must be refactored to Kotlin Multiplatform or rewritten.
