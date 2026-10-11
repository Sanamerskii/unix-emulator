#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./fixtures.sh
./run.sh --vfs build/vfs/deep.zip --script demos/stage-4-success.txt
for script in unknown parser variable ls cd head du exit environment; do
    ./run.sh --vfs build/vfs/deep.zip --script "demos/errors/$script.txt"
done
./run.sh --vfs build/vfs/deep.zip --script demos/exit.txt
