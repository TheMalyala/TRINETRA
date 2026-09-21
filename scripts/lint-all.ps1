# Lint check script for Trinetra
Write-Host "Running Trinetra repository lint checks..." -ForegroundColor Cyan

# Check for hard-coded colors in Compose files
$composeFiles = Get-ChildItem -Path "android" -Recurse -Filter "*.kt" | Where-Object { $_.FullName -notmatch "core[\\/]designsystem" }
$failed = $false
foreach ($f in $composeFiles) {
    $content = Get-Content $f.FullName -Raw
    if ($content -match "Color\(0x|Color\.Red|Color\.Green|Color\.Blue") {
        Write-Host "[ERROR] Hardcoded color found in: $($f.FullName)" -ForegroundColor Red
        $failed = $true
    }
}

if (-not $failed) {
    Write-Host "[PASS] Monochrome design lint clean (no hardcoded colors found)." -ForegroundColor Green
}

# Run Ruff if available (check virtual environment first)
$ruffCmd = if (Test-Path "backend\.venv\Scripts\ruff.exe") {
    "backend\.venv\Scripts\ruff.exe"
} elseif (Get-Command ruff -ErrorAction SilentlyContinue) {
    "ruff"
} else {
    $null
}

if ($ruffCmd) {
    Write-Host "Running Ruff on backend via $ruffCmd..." -ForegroundColor Cyan
    & $ruffCmd check backend/
    if ($LASTEXITCODE -ne 0) {
        $failed = $true
    }
}

Write-Host "Lint checks complete." -ForegroundColor Cyan
