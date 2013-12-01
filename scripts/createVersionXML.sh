#!/bin/bash

set -e

while getopts c:d OPTION "$@"; do
    case $OPTION in
	c)
	    COMPONENT_DIR=${OPTARG}
	    ;;
	d)
	    set -x
	    ;;
    esac
done

if [[ -z "$COMPONENT_DIR" ]]; then
    echo "usage: `basename $0` -c <componentDirectory> [-d]"
    exit 1
fi

VERSION_NAME=$(awk -F'"' '/android:versionName/{print $(NF-1); exit}' ${COMPONENT_DIR}/AndroidManifest.xml)

if command -v git &> /dev/null && [[ -d ${COMPONENT_DIR}/../.git ]]; then
    GIT_REF=$(git describe --tags --dirty=+)
    # Only add the result of git describe if it's not the same string a $VERSION_NAME
    if [[ "$VERSION_NAME" != "$GIT_REF" ]]; then
	VERSION_NAME+=" (${GIT_REF})"
    fi
fi

cat <<EOF > ${COMPONENT_DIR}/res/values/version.xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="version">${VERSION_NAME}</string>
</resources>
EOF
