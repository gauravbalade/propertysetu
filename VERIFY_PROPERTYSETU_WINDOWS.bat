@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\verify-propertysetu-windows.ps1"
set "RESULT=%ERRORLEVEL%"
echo.
if "%RESULT%"=="0" (
  echo PropertySetu verification completed successfully.
) else (
  echo PropertySetu verification reported one or more failures. Read the messages above.
)
pause
exit /b %RESULT%
