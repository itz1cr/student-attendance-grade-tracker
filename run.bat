@echo off
rem ============================================================
rem  Student Attendance & Grade Tracker - run the application
rem  Creates the database and demo data on the first run.
rem ============================================================
setlocal
cd /d "%~dp0"

call compile.bat
if errorlevel 1 exit /b 1

echo Starting the application...
java -cp "out;lib\sqlite-jdbc.jar" com.aat.tracker.Main
endlocal
