@echo off
REM NetGuard - Build Script for Windows
REM Compiles and runs the NetGuard Network Monitoring System

echo ╔══════════════════════════════════════════════════════════════╗
echo ║         NetGuard - Build Script                             ║
echo ║         Adapter + Bridge Design Patterns Demo               ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

REM Check if Java is available
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install JDK 8 or higher
    pause
    exit /b 1
)

REM Create output directory
if not exist out (
    echo Creating output directory...
    mkdir out
)

echo.
echo [1/2] Compiling Java sources...
echo.

REM Compile all Java files
javac -d out -encoding UTF-8 src\netguard\Main.java src\netguard\devices\NetworkDevice.java src\netguard\devices\DeviceMetrics.java src\netguard\devices\vendor\*.java src\netguard\devices\adapters\*.java src\netguard\notifications\*.java src\netguard\alerts\*.java src\netguard\core\*.java src\netguard\gui\*.java

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed!
    pause
    exit /b 1
)

echo.
echo [2/2] Compilation successful!
echo.

echo Starting NetGuard Dashboard...
echo.

REM Run the application
java -cp out netguard.Main

pause
