#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/test-classes
javac -encoding UTF-8 --release 17 -d build/test-classes src/emulator/*.java tests/emulator/*.java
exec java -Djava.awt.headless=true -cp build/test-classes emulator.ShellTest
