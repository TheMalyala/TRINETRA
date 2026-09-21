#!/usr/bin/env bash
# Verifies SHA-256 integrity of an Advice Ledger JSON export
set -e

if [ -z "$1" ]; then
    echo "Usage: $0 <advice_ledger_export.json>"
    exit 1
fi

python3 -c '
import sys, json, hashlib

with open(sys.argv[1]) as f:
    entries = json.load(f)

prev_hash = "0" * 64
for idx, entry in enumerate(entries):
    assert entry["prev_hash"] == prev_hash, f"Chain broken at entry {idx}!"
    body = {k: v for k, v in entry.items() if k not in ["hash", "signature"]}
    canonical = json.dumps(body, sort_keys=True)
    computed_hash = hashlib.sha256((prev_hash + canonical).encode()).hexdigest()
    assert computed_hash == entry["hash"], f"Hash mismatch at entry {idx}!"
    prev_hash = entry["hash"]

print(f"[OK] Audit chain verified: {len(entries)} entries intact.")
' "$1"
