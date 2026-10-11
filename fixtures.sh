#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/fixture-classes
javac -encoding UTF-8 --release 17 -d build/fixture-classes tests/emulator/FixtureBuilder.java
exec java -cp build/fixture-classes emulator.FixtureBuilder
