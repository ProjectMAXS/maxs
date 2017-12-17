#!/bin/bash

# Source the config files
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

MODULES=$(find $BASEDIR -mindepth 1 -maxdepth 1 -type d -name 'module-*')

for m in $MODULES ; do
    pkg=$(getPackageOfComponent "$m")
    echo "Removing $pkg from device"
    adb shell pm uninstall $pkg
done
