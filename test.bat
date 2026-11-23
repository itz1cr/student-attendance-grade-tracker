@echo off
rem ============================================================
rem  Student Attendance & Grade Tracker - smoke tests
rem  Runs the console test suite against an in-memory database.
rem ============================================================
setlocal
cd /d "%~dp0"

call compile.bat
if errorlevel 1 exit /b 1

echo Running smoke tests...
java -cp "out;lib\sqlite-jdbc.jar" com.aat.tracker.SmokeTest
endlocal
