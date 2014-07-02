#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/functions.sh"

MODULES=$(find $BASEDIR -mindepth 1 -maxdepth 1 -type d -name 'module-*')

for m in $MODULES ; do
    pkg=$(get_package $m)
    echo "Removing $pkg from device"
    adb shell pm uninstall $pkg
done
