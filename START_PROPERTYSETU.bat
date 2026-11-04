@echo off
setlocal
cd /d "%~dp0"

echo.
echo  PropertySetu - Local Development Launcher
echo  ------------------------------------------
echo  Start MySQL first. The database must exist.
echo  Backend API : http://localhost:8080
echo  Frontend    : http://localhost:5173
echo.

start "PropertySetu Backend" powershell.exe -NoLogo -NoExit -ExecutionPolicy Bypass -File "%~dp0scripts\run-backend-local.ps1"
start "PropertySetu Frontend" powershell.exe -NoLogo -NoExit -ExecutionPolicy Bypass -File "%~dp0scripts\run-frontend-local.ps1"

timeout /t 5 /nobreak >nul
start "" "http://localhost:5173"

echo Two terminal windows were opened.
echo Keep both running while using the application.
echo Close each window or press Ctrl+C in it to stop its service.
endlocal
