$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $repoRoot "backend"
$envFile = Join-Path $backendDir ".env.local"

function Import-LocalEnvFile([string]$path) {
    if (-not (Test-Path $path)) { return }
    Get-Content $path | ForEach-Object {
        $line = $_.Trim()
        $separatorIndex = $line.IndexOf("=")
        if (-not $line -or $line.StartsWith("#") -or $separatorIndex -le 0) { return }
        $name = $line.Substring(0, $separatorIndex).Trim()
        $value = $line.Substring($separatorIndex + 1).Trim()
        if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        if ($name -match '^[A-Za-z_][A-Za-z0-9_]*$' -and [string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
            Set-Item -Path "Env:$name" -Value $value
        }
    }
}

Import-LocalEnvFile $envFile

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "Java was not found. Install JDK 17 or newer, set JAVA_HOME, and reopen this terminal."
}

if ([string]::IsNullOrWhiteSpace($env:DB_URL)) {
    $env:DB_URL = "jdbc:mysql://localhost:3306/property_registration_db?useSSL=false&serverTimezone=Asia/Kolkata"
}
if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) { $env:DB_USERNAME = "root" }
if ([string]::IsNullOrWhiteSpace($env:SERVER_PORT)) { $env:SERVER_PORT = "8080" }
if ([string]::IsNullOrWhiteSpace($env:DDL_AUTO)) { $env:DDL_AUTO = "update" }
if ([string]::IsNullOrWhiteSpace($env:FRONTEND_ORIGIN)) { $env:FRONTEND_ORIGIN = "http://localhost:5173" }
if ([string]::IsNullOrWhiteSpace($env:UPLOAD_DIR)) { $env:UPLOAD_DIR = Join-Path $repoRoot "demo\uploads" }
if ([string]::IsNullOrWhiteSpace($env:RAZORPAY_KEY_ID)) { $env:RAZORPAY_KEY_ID = "" }
if ([string]::IsNullOrWhiteSpace($env:RAZORPAY_KEY_SECRET)) { $env:RAZORPAY_KEY_SECRET = "" }
if ([string]::IsNullOrWhiteSpace($env:RAZORPAY_WEBHOOK_SECRET)) { $env:RAZORPAY_WEBHOOK_SECRET = "" }

# Resolve relative upload paths from the backend directory, not the caller's shell directory.
if (-not [System.IO.Path]::IsPathRooted($env:UPLOAD_DIR)) {
    $env:UPLOAD_DIR = [System.IO.Path]::GetFullPath((Join-Path $backendDir $env:UPLOAD_DIR))
}

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET) -or $env:JWT_SECRET.Length -lt 32) {
    $bytes = New-Object byte[] 48
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $env:JWT_SECRET = -join ($bytes | ForEach-Object { $_.ToString("x2") })
    Write-Host "Generated an ephemeral local JWT signing secret for this run." -ForegroundColor Green
}

if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
    $securePassword = Read-Host "MySQL password for $($env:DB_USERNAME) (press Enter if none)" -AsSecureString
    $ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
    } finally {
        [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
    }
}

if ($env:DB_URL -match '^jdbc:mysql://([^:/?]+)(?::([0-9]+))?') {
    $dbHost = $Matches[1]
    $dbPort = if ($Matches[2]) { [int]$Matches[2] } else { 3306 }
    $probe = Test-NetConnection -ComputerName $dbHost -Port $dbPort -WarningAction SilentlyContinue
    if (-not $probe.TcpTestSucceeded) {
        throw ("Cannot reach MySQL at " + $dbHost + ":" + $dbPort + ". Start MySQL, create property_registration_db, or correct DB_URL in backend\.env.local.")
    }
}

New-Item -ItemType Directory -Force -Path $env:UPLOAD_DIR | Out-Null
Set-Location $backendDir
Write-Host "Starting PropertySetu API at http://localhost:$($env:SERVER_PORT)" -ForegroundColor Cyan
Write-Host "Health endpoint: http://localhost:$($env:SERVER_PORT)/api/health"
Write-Host "Razorpay checkout requires valid rzp_test_ credentials."
& .\mvnw.cmd spring-boot:run
if ($LASTEXITCODE -ne 0) {
    Write-Host "Backend stopped with exit code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}
