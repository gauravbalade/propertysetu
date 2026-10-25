# PropertySetu Windows verification helper
# Safe: runs checks in the current checkout and does not edit application source files.
$ErrorActionPreference = "Stop"

function Write-Section([string]$Title) {
    Write-Host ""
    Write-Host "=== $Title ===" -ForegroundColor Cyan
}

function Require-Command([string]$Name, [string]$InstallHint) {
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "$Name was not found. $InstallHint"
    }
}

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Frontend = Join-Path $RepoRoot "frontend"
$Backend = Join-Path $RepoRoot "backend"

Write-Section "PropertySetu safe verification"
Write-Host "Repository: $RepoRoot"
Write-Host "This script does not change application source or push commits."

Require-Command "git" "Install Git for Windows, then reopen PowerShell."
Require-Command "node" "Install Node.js 22 LTS, then reopen PowerShell."
Require-Command "npm" "Install Node.js 22 LTS, then reopen PowerShell."
Require-Command "java" "Install a JDK 17, then reopen PowerShell."

Write-Host ("Node: " + (node --version))
Write-Host ("npm:  " + (npm --version))
Write-Host ("Java: " + ((java -version 2>&1 | Select-Object -First 1).ToString()))

$Failures = New-Object System.Collections.Generic.List[string]

Write-Section "Frontend install and production build"
if (-not (Test-Path (Join-Path $Frontend "package-lock.json"))) {
    $Failures.Add("Frontend package-lock.json is missing.")
} else {
    Push-Location $Frontend
    try {
        npm ci
        if ($LASTEXITCODE -ne 0) { throw "npm ci failed with exit code $LASTEXITCODE" }
        npm run lint
        if ($LASTEXITCODE -ne 0) { throw "npm run lint failed with exit code $LASTEXITCODE" }
        Write-Host "Frontend lint: PASS" -ForegroundColor Green
        npm run build
        if ($LASTEXITCODE -ne 0) { throw "npm run build failed with exit code $LASTEXITCODE" }
        Write-Host "Frontend production build: PASS" -ForegroundColor Green
    } catch {
        $Failures.Add("Frontend: " + $_.Exception.Message)
    } finally {
        Pop-Location
    }
}

Write-Section "Backend Maven package"
$MavenWrapper = Join-Path $Backend "mvnw.cmd"
if (-not (Test-Path $MavenWrapper)) {
    $Failures.Add("Backend Maven wrapper mvnw.cmd is missing.")
} else {
    Push-Location $Backend
    try {
        # Tests may require a configured local MySQL database and environment variables.
        # This command is intentionally run as-is so the repository's actual config is respected.
        & $MavenWrapper -B test package
        if ($LASTEXITCODE -ne 0) {
            $Failures.Add("Backend Maven test/package failed. Review the first Maven error; database/environment setup may be required.")
        } else {
            Write-Host "Backend Maven test/package: PASS" -ForegroundColor Green
        }
    } catch {
        $Failures.Add("Backend: " + $_.Exception.Message)
    } finally {
        Pop-Location
    }
}

Write-Section "Result"
if ($Failures.Count -gt 0) {
    Write-Host "Verification did not fully pass:" -ForegroundColor Yellow
    $Failures | ForEach-Object { Write-Host (" - " + $_) -ForegroundColor Yellow }
    Write-Host "No application source files were changed by this script."
    exit 1
}

Write-Host "All available checks passed." -ForegroundColor Green
exit 0
