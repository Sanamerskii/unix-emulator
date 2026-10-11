#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./fixtures.sh
for vfs in minimal several deep; do
    ./run.sh --vfs "build/vfs/$vfs.zip" --script demos/stage-3-success.txt
done
./run.sh --vfs 'build/vfs/VFS with spaces.zip' --script demos/stage-3-success.txt
