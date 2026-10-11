#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./fixtures.sh
if ./run.sh --vfs build/vfs/missing.zip; then exit 1; fi
if ./run.sh --vfs build/vfs/invalid.zip; then exit 1; fi
./run.sh --vfs build/vfs/deep.zip --script demos/stage-2-error.txt
./run.sh --vfs build/vfs/several.zip --script build/missing-script.txt
./run.sh --vfs build/vfs/minimal.zip --script demos/exit.txt
