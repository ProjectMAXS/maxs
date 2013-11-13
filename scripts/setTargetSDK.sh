#!/bin/bash

set -e

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

TARGET_SDK=

while getopts ds: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	s)
	   TARGET_SDK="$OPTARG"
	   ;;
    esac
done

if [[ -z $TARGET_SDK ]]; then
    echo "usage: `basename $0` [-d] -s <targetSDK>"
    exit 1
fi

setTargetSDK() {
    local targetSDK=$1
    local component=$2

    sed -i "s/android:targetSdkVersion=\"[^\"]*\"/android:targetSdkVersion=\"${targetSDK}\"/" ${component}/AndroidManifest.xml
    sed -i "s/target=android-.*/target=android-${targetSDK}/" ${component}/project.properties
}

setTargetSDK $TARGET_SDK $MAINDIR
for t in $TRANSPORTS ; do
    setTargetSDK $TARGET_SDK $t
done
for m in $MODULES ; do
    setTargetSDK $TARGET_SDK $m
done
