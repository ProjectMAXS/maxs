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
Categories:
  - System
License: GPL-3.0-or-later
Web Site: http://projectmaxs.org
Source Code: http://projectmaxs.org/source
Issue Tracker: http://projectmaxs.org/issues
Donate: http://projectmaxs.org/donate
Bitcoin: bc1qu482c0tngkcvx3q7mrm8zmuldrh2f2lrh26ym0

Auto Name: MAXS Module ${moduleName}
Summary: A Module for MAXS
Description:
This is a Module for MAXS, which does not install any launcher.
You need "MAXS Main" and a configured MAXS Transport to make use of it.
.

Repo Type: srclib
Repo: ProjectMAXS

Builds:
 - versionName: ${versionName}
   versionCode: ${versionCode}
   commit: ${versionName}
   subdir: ${MODULE}
   submodules: true
   sudo: apt-get install -y imagemagick libxml2-utils
   init: make -C .. ${MODULE}/Makefile
   gradle:
     - yes
   rm:
     - module-shell/libraryProjects/root-commands/ExampleApp

Auto Update Mode: Version %v
Update Check Mode: Tags
Current Version: ${versionName}
Current Version Code: ${versionCode}
EOF
