#!/bin/sh
# Student Attendance & Grade Tracker - compile and run smoke tests (Linux/macOS)
set -e
cd "$(dirname "$0")"
mkdir -p out data
javac -encoding UTF-8 -cp "lib/sqlite-jdbc.jar:lib/*" -d out \
  $(find src test -name '*.java')
java -cp "out:lib/sqlite-jdbc.jar:lib/*" com.aat.tracker.SmokeTest
