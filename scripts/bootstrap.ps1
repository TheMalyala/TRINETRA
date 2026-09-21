# Windows Bootstrap Script for Trinetra
Write-Host "Bootstrapping TRINETRA environment..." -ForegroundColor Cyan

# 1. Verify Python
if (Get-Command python -ErrorAction SilentlyContinue) {
    $pyVersion = python --version
    Write-Host "[OK] Python detected: $pyVersion" -ForegroundColor Green
} else {
    Write-Host "[WARN] Python not found in PATH. Please install Python 3.12+." -ForegroundColor Yellow
}

# 2. Verify Android SDK / Java
if ($env:ANDROID_HOME -or $env:JAVA_HOME) {
    Write-Host "[OK] Android/Java environment variables found." -ForegroundColor Green
} else {
    Write-Host "[INFO] Note: Keep Android Studio installed for native emulator and SDK tooling." -ForegroundColor Gray
}

# 3. Create .env if missing
if (-not (Test-Path "infra/secrets/.env")) {
    Copy-Item "infra/secrets/.env.example" "infra/secrets/.env"
    Write-Host "[OK] Created infra/secrets/.env from template." -ForegroundColor Green
}

Write-Host "Bootstrap complete. Ready for Phase 0 execution." -ForegroundColor Cyan
