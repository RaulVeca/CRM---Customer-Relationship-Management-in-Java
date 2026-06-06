@echo off
REM CRM Training IT - Quick Start Script

echo.
echo ================================================
echo   CRM Training IT - Quick Start
echo ================================================
echo.

where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven nu este instalat!
    pause
    exit /b 1
)

echo [1/4] Verificare Maven...
mvn -version
echo.

echo [2/4] Build proiect...
call mvn clean install
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build esuat!
    pause
    exit /b 1
)
echo.

echo [3/4] Creare directoare...
if not exist "data" mkdir data
if not exist "logs" mkdir logs
echo.

echo [4/4] Pornire aplicatie...
echo.
java -jar target\crm-system-jar-with-dependencies.jar

pause
