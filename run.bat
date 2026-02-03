@echo off
REM Advertising Company Management System - Run Script (Windows)
REM This script compiles and runs the JavaFX application

echo =========================================
echo Advertising Company Management System
echo =========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven is not installed!
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java is not installed!
    echo Please install Java JDK 17+ from: https://adoptium.net/
    pause
    exit /b 1
)

echo Cleaning and compiling the project...
call mvn clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Compilation successful! Starting application...
    echo.
    call mvn javafx:run
) else (
    echo.
    echo ERROR: Compilation failed. Please check the error messages above.
    pause
    exit /b 1
)

pause
