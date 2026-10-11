#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./run.sh --vfs build/demo.zip --script demos/stage-2-error.txt
./run.sh --vfs 'build/VFS with spaces.zip' --script build/missing-script.txt
./run.sh --vfs build/demo.zip --script demos/exit.txt
