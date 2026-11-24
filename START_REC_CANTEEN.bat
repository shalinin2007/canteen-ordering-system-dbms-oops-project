@echo off
setlocal
title REC Canteen - Rajalakshmi Engineering College
color 0A
cls

set "PROJECT_DIR=%~dp0"
set "MVN_CMD="

if exist "%USERPROFILE%\maven\apache-maven-3.9.6\bin\mvn.cmd" set "MVN_CMD=%USERPROFILE%\maven\apache-maven-3.9.6\bin\mvn.cmd"
if not defined MVN_CMD (
    for /f "delims=" %%M in ('where mvn.cmd 2^>nul') do if not defined MVN_CMD set "MVN_CMD=%%M"
)

echo.
echo  ====================================================
echo    REC CANTEEN - Rajalakshmi Engineering College
echo    Launching Website...
echo  ====================================================
echo.

echo [STEP 1] Checking Java...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java was not found. Install Java 17 or later and add it to PATH.
    pause
    exit /b 1
)
echo Java OK!

echo.
if not defined MVN_CMD (
    echo ERROR: Maven was not found.
    echo Install Maven or add mvn.cmd to PATH.
    pause
    exit /b 1
)

echo.
echo [STEP 2] Starting the Spring Boot application...
start "REC Canteen Server" /D "%PROJECT_DIR%" cmd.exe /k ""%MVN_CMD%" spring-boot:run"

echo.
echo [STEP 3] Waiting for the menu API to become ready...
set "ATTEMPT=0"
:wait_health
set "HTTP_CODE="
for /f %%H in ('curl.exe -s -o NUL -w "%%{http_code}" --max-time 3 http://localhost:8080/api/menu 2^>nul') do set "HTTP_CODE=%%H"
if "%HTTP_CODE%"=="200" goto :ready
set /a ATTEMPT+=1
if %ATTEMPT% GEQ 60 goto :failed
timeout /t 2 /nobreak >nul
goto :wait_health

:ready
echo.
echo REC Canteen is ready at http://localhost:8080
echo Student Login : 22cse001@rec.edu.in / student123
echo Admin Login   : admin@rec.edu.in / admin123
start "" "http://localhost:8080"
pause
exit /b 0

:failed
echo.
echo ERROR: REC Canteen did not become ready within 2 minutes.
echo Check the REC Canteen Server window for the startup error.
pause
exit /b 1

:unhealthy
echo.
echo ERROR: Something is responding on port 8080, but the menu API returned HTTP %HTTP_CODE%.
echo Check the existing server before trying to start another instance.
pause
exit /b 1
