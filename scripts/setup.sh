#!/usr/bin/env bash
set -e

# Do not use getopts here, because this script is sourced by others,
# which very likely will have other argument parameters as this one.
for OPTARG in "$@"; do
    if [[ $OPTARG == "-d" ]]; then
	set -x
    fi
done

# Reset OPTIND because setup.sh may be sourced from other scripts that
# also use getopts
OPTIND=1

# Pretty fancy method to get reliable the absolute path of a shell
# script, *even if it is sourced*. Credits go to GreenFox on
# stackoverflow: http://stackoverflow.com/a/12197518/194894
pushd . > /dev/null
SCRIPTDIR="${BASH_SOURCE[0]}";
while([ -h "${SCRIPTDIR}" ]); do
    cd "`dirname "${SCRIPTDIR}"`"
    SCRIPTDIR="$(readlink "`basename "${SCRIPTDIR}"`")";
done
cd "`dirname "${SCRIPTDIR}"`" > /dev/null
SCRIPTDIR="`pwd`";
popd  > /dev/null

. "${SCRIPTDIR}/functions.sh"

BASEDIR="$(cd ${SCRIPTDIR}/.. && pwd)"
HOMEPAGE="${BASEDIR}/homepage"
DOCDIR="${BASEDIR}/documentation"
MAINDIR="${BASEDIR}/main"
TRANSPORTS="$(find $BASEDIR -mindepth 1 -maxdepth 1 -type d -name 'transport-*')"
MODULES="$(find $BASEDIR -mindepth 1 -maxdepth 2 -path '*module-*' -name AndroidManifest.xml -printf '%h\n')"
COMPONENTS="${MAINDIR} ${TRANSPORTS} ${MODULES}"

set +e
if ! command -v xmllint &> /dev/null; then
    declare -A MOD2PKG
    for m in $MODULES ; do
	module_name=$(basename $m)
	module_package=$(xmlstarlet sel -t -v "//manifest/@package" ${m}/AndroidManifest.xml)
	MOD2PKG[${module_name}]=${module_package}
    done
else
	echoerr "WARNING: xmllint not found! Some things may not work"
fi
set -e

if [[ -f ${BASEDIR}/config ]]; then
    # config is there, source it
    . ${BASEDIR}/config
    # and set further env variables based on the config
    FDROIDMETA="${FDROIDDATA}/metadata"
fi
