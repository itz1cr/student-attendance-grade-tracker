@echo off
rem ============================================================
rem  Student Attendance & Grade Tracker - screenshot generator
rem  Writes one PNG per application screen into docs/screenshots.
rem ============================================================
setlocal
cd /d "%~dp0"

call compile.bat
if errorlevel 1 exit /b 1

echo Generating screenshots...
java -cp "out;lib\sqlite-jdbc.jar" com.aat.tracker.ui.ScreenshotGenerator
endlocal
