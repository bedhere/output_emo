@echo off
echo ========================================
echo   Emotion Kaomoji Input Method
echo ========================================
echo.

cd /d "%~dp0"

echo [INFO] Checking Java environment...
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java not found. Please install Java 11 or higher.
    echo [INFO] Download: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Checking Maven environment...
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven not found. Please install Maven.
    echo [INFO] Download: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

echo [INFO] Environment OK
echo [INFO] Starting application...
echo.

mvn javafx:run

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start.
    echo [INFO] Try running manually: mvn clean javafx:run
    pause
)
