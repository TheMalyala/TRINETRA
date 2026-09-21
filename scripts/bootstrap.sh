#!/usr/bin/env bash
set -e

echo "Bootstrapping TRINETRA environment..."

if command -v python3 >/dev/null 2>&1; then
    echo "[OK] Python detected: $(python3 --version)"
else
    echo "[WARN] Python3 not found in PATH."
fi

if [ ! -f "infra/secrets/.env" ]; then
    cp infra/secrets/.env.example infra/secrets/.env
    echo "[OK] Created infra/secrets/.env from template."
fi

echo "Bootstrap complete."
