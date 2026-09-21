#!/usr/bin/env bash
set -e

echo "Running Trinetra repository lint checks..."

# Check for hardcoded colors in Android Kotlin files outside designsystem
if grep -rn --exclude-dir=core/designsystem -E "Color\(0x|Color\.Red|Color\.Green|Color\.Blue" android/ 2>/dev/null; then
    echo "[ERROR] Hardcoded color found in Android source."
    exit 1
else
    echo "[PASS] Monochrome design lint clean."
fi

if command -v ruff >/dev/null 2>&1; then
    echo "Running Ruff on backend..."
    ruff check backend/
fi

echo "Lint checks complete."
