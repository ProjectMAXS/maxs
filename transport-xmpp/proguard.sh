#!/bin/bash
set -x
PROGUARD_STRING="proguard.config=proguard.cfg"

proguard_enabled() {
    return $(grep $PROGUARD_STRING local.properties &> /dev/null)
}

enabled=notset

[ ! -z $PROGUARD_ENABLED ] && enabled=$PROGUARD_ENABLED
[ ! -z $1 ] && enabled=$1

if $enabled && ! proguard_enabled ; then
    echo $PROGUARD_STRING >> local.properties
elif ! $enabled && proguard_enabled ; then
    sed -i "/${PROGUARD_STRING}/d" local.properties
fi
