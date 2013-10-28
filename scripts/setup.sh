#!/bin/bash

SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASEDIR="$(cd ${SCRIPTDIR}/.. && pwd)"
HOMEPAGE="${BASEDIR}/homepage"
DOCDIR="${BASEDIR}/documentation"
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

if [[ ! -f ${BASEDIR}/config ]]; then
    echo "config not found"
else
    # config is there, source it
    . ${BASEDIR}/config
    # and set further env variables based on the config
    FDROIDMETA="${FDROIDDATA}/metadata"
fi
