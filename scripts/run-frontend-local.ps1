$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $repoRoot "frontend"
$envFile = Join-Path $frontendDir ".env.local"

if (-not (Get-Command node -ErrorAction SilentlyContinue) -or -not (Get-Command npm -ErrorAction SilentlyContinue)) {
    throw "Node.js LTS and npm are required. Install Node.js, reopen this terminal, and try again."
}

if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#") -or -not $line.Contains("=")) { return }
        $parts = $line.Split("=", 2)
        $name = $parts[0].Trim()
        $value = $parts[1].Trim()
        if ($name -match '^VITE_[A-Za-z0-9_]+$' -and [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}
if ([string]::IsNullOrWhiteSpace($env:VITE_API_URL)) { $env:VITE_API_URL = "http://localhost:8080" }

Set-Location $frontendDir
if (-not (Test-Path (Join-Path $frontendDir "node_modules"))) {
    Write-Host "Installing exact frontend dependencies from package-lock.json..." -ForegroundColor Cyan
    npm ci
    if ($LASTEXITCODE -ne 0) { throw "npm ci failed. Check the npm output above." }
}
Write-Host "Starting PropertySetu frontend at http://localhost:5173" -ForegroundColor Cyan
npm run dev -- --host 0.0.0.0
if ($LASTEXITCODE -ne 0) {
    Write-Host "Frontend stopped with exit code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}
