@echo off
title Medicine Expiry and Inventory Tracker
echo ===================================================
echo   Medicine Expiry and Inventory Tracker (Core Java)
echo ===================================================
echo Compiling source files...
javac *.java
if %errorlevel% neq 0 (
    echo Compilation failed! Please check for errors above.
    pause
    exit /b %errorlevel%
)
echo Compilation successful. Starting application...
echo.
java Main
pause
