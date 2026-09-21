param(
    [Parameter(Mandatory=$true)]
    [string]$ExportPath
)

$pyCmd = if (Test-Path "backend\.venv\Scripts\python.exe") {
    "backend\.venv\Scripts\python.exe"
} elseif (Get-Command python -ErrorAction SilentlyContinue) {
    "python"
} else {
    Write-Host "[ERROR] Python not found." -ForegroundColor Red
    exit 1
}

& $pyCmd -c @"
import sys, json, hashlib

with open(r'$ExportPath') as f:
    entries = json.load(f)

prev_hash = '0' * 64
for idx, entry in enumerate(entries):
    assert entry['prev_hash'] == prev_hash, f'Chain broken at entry {idx}!'
    body = {k: v for k, v in entry.items() if k not in ['hash', 'signature']}
    canonical = json.dumps(body, sort_keys=True)
    computed_hash = hashlib.sha256((prev_hash + canonical).encode()).hexdigest()
    assert computed_hash == entry['hash'], f'Hash mismatch at entry {idx}!'
    prev_hash = entry['hash']

print(f'[OK] Audit chain verified: {len(entries)} entries intact.')
"@
