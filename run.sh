#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
javac -encoding UTF-8 --release 17 -d build/classes src/emulator/*.java
exec java -cp build/classes emulator.Main "$@"
