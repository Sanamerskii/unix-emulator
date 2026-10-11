#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./fixtures.sh
./run.sh --vfs build/vfs/deep.zip --script demos/stage-5-success.txt
for script in touch chown; do
    ./run.sh --vfs build/vfs/deep.zip --script "demos/errors/$script.txt"
done
# Новая загрузка показывает исходные данные: созданные файлы и владельцы не сохранялись.
./run.sh --vfs build/vfs/deep.zip --script demos/stage-4-success.txt
