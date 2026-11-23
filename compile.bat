@echo off
rem ============================================================
rem  Student Attendance & Grade Tracker - compile script
rem  Compiles every source file (app + smoke tests) with javac.
rem ============================================================
setlocal
cd /d "%~dp0"

if not exist out mkdir out
if not exist data mkdir data

javac -encoding UTF-8 -cp "lib\sqlite-jdbc.jar" -d out ^
  src\com\aat\tracker\Main.java ^
  src\com\aat\tracker\model\*.java ^
  src\com\aat\tracker\exception\*.java ^
  src\com\aat\tracker\util\*.java ^
  src\com\aat\tracker\db\*.java ^
  src\com\aat\tracker\dao\*.java ^
  src\com\aat\tracker\service\*.java ^
  src\com\aat\tracker\ui\*.java ^
  test\com\aat\tracker\SmokeTest.java

if errorlevel 1 (
  echo.
  echo Compilation FAILED. Fix the errors above and run compile.bat again.
  exit /b 1
)
echo.
echo Compilation successful. Classes written to the "out" folder.
endlocal
