#!/bin/bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR="$(cd ${SCRIPTDIR}/.. && pwd)"
HOMEPAGE="${BASEDIR}/homepage"

if [[ ! -f ${BASEDIR}/config ]]; then
    echo "config not found"
    exit 1
fi

. ${BASEDIR}/config

FDROIDMETA="${FDROIDDATA}/metadata"
MAINDIR="${BASEDIR}/main"
TRANSPORTS="$(find $BASEDIR -mindepth 1 -maxdepth 1 -type d -name 'transport-*')"
MODULES="$(find $BASEDIR -mindepth 1 -maxdepth 1 -type d -name 'module-*')"
COMPONENTS="${MAINDIR} ${TRANSPORTS} ${MODULES}"

declare -A MOD2PKG

for m in $MODULES ; do
    module_name=$(basename $m)
    module_package=$(xml sel -t -v "//manifest/@package" ${m}/AndroidManifest.xml)
    MOD2PKG[${module_name}]=${module_package}
done
