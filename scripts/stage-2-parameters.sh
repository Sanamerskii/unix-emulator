#!/bin/sh
# Закрывайте каждое окно, чтобы перейти к следующему примеру.
set -eu
cd "$(dirname "$0")/.."
./run.sh
./run.sh --vfs 'build/VFS with spaces.zip'
./run.sh --script demos/stage-2-success.txt
./run.sh --vfs build/demo.zip --script demos/stage-2-success.txt
if ./run.sh --unknown; then exit 1; fi
if ./run.sh --vfs; then exit 1; fi
if ./run.sh --script ''; then exit 1; fi
if ./run.sh --vfs first --vfs second; then exit 1; fi
