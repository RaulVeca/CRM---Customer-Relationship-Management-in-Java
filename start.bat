@echo off
REM CRM Training IT - Quick Start Script

echo.
echo ================================================
echo   CRM Training IT - Quick Start
echo ================================================
echo.

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or is not available in PATH.
    pause
    exit /b 1
)

echo [1/4] Checking Maven...
mvn -version
echo.

echo [2/4] Building project...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed.
    pause
    exit /b 1
)
echo.

echo [3/4] Creating directories...
if not exist "data" mkdir data
if not exist "logs" mkdir logs
echo.

echo [4/4] Starting application...
echo.
java -jar target\crm-system-jar-with-dependencies.jar

pause
