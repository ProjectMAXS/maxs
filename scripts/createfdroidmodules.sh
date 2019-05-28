#!/usr/bin/env bash

set -e

# Source the config files
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

while getopts dm: OPTION "$@"; do
	case $OPTION in
		d)
			set -x
			;;
		m)
			MODULE=${OPTARG}
			;;
		esac
done

if [[ -z $MODULE ]]; then
	echo "usage: `basename $0` [-d] -m <moduleName>"
	exit 1
fi

versionCode=$(getVersionCodeFromManifest ${BASEDIR}/${MODULE}/AndroidManifest.xml)
versionName=$(getVersionNameFromManifest ${BASEDIR}/${MODULE}/AndroidManifest.xml)
moduleName=${MODULE#module-}

cat <<EOF > ${FDROIDMETA}/${MOD2PKG[${MODULE}]}.txt
Categories:System
License:GPLv3
Web Site:http://projectmaxs.org
Source Code:http://projectmaxs.org/source
Issue Tracker:http://projectmaxs.org/issues
Donate:http://projectmaxs.org/donate
Bitcoin:bc1qu482c0tngkcvx3q7mrm8zmuldrh2f2lrh26ym0

Auto Name:MAXS Module ${moduleName}
Summary:A Module for MAXS
Description:
This is a Module for MAXS, which does not install any launcher.
You need "MAXS Main" and a configured MAXS Transport to make use of it.
.

Repo Type:srclib
Repo:ProjectMAXS

Build:${versionName},${versionCode}
    commit=${versionName}
    subdir=${MODULE}
    submodules=yes
    sudo=apt-get install -y python-gnupg libxml2-utils
    init=cd .. && \\
        make ${MODULE}/Makefile
    prebuild=make prebuild && \
        rm -rf ../module-shell/libraryProjects/root-commands/ExampleApp

Auto Update Mode:Version %v
Update Check Mode:Tags
Current Version:${versionName}
Current Version Code:${versionCode}
EOF
