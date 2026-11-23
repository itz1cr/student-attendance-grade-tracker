@echo off
rem ============================================================
rem  Publish this project to GitHub.
rem
rem  Run this from YOUR OWN Command Prompt / PowerShell terminal
rem  (NOT from the DeepSeek Harness sandbox, which blocks the
rem  TLS connection to github.com).
rem
rem  Usage:
rem    publish.bat  https://github.com/USERNAME/student-attendance-grade-tracker.git
rem    (or just run it - it will prompt you for the URL)
rem
rem  The first push opens a browser to sign in to GitHub.
rem ============================================================
setlocal
cd /d "%~dp0"

set "REMOTE=%1"
if "%REMOTE%"=="" (
    set /p REMOTE="Paste your GitHub repo URL (https://github.com/USER/repo.git): "
)

if "%REMOTE%"=="" (
    echo No URL provided. Aborting.
    exit /b 1
)

git remote remove origin 2>nul
git remote add origin "%REMOTE%"
echo.
echo Pushing branch "main" to %REMOTE% ...
git push -u origin main

if errorlevel 1 (
    echo.
    echo Push failed. If git asked for a username/password, use a Personal
    echo Access Token as the password: github.com -^> Settings -^> Developer
    echo settings -^> Personal access tokens -^> Tokens (classic).
) else (
    echo.
    echo Done! Your repository is now public at %REMOTE%
)
endlocal
