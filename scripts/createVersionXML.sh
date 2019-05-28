#!/usr/bin/env bash

set -e

# Source the config files
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

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
    echo "usage: $(basename "$0") -c <componentDirectory> [-d]"
    exit 1
fi

TMPDIR=$(mktemp -d)
trap 'rm -rf ${TMPDIR}' EXIT

VERSION_NAME=$(getVersionNameFromManifest "${COMPONENT_DIR}/AndroidManifest.xml")

if command -v git &> /dev/null && [[ -d ${COMPONENT_DIR}/../.git ]]; then
    GIT_REF=$(git describe --tags --dirty=+)
    set +e
    GIT_SYMBOLIC_REF=$(git symbolic-ref --short HEAD)
    set -e
    if [[ $? -eq 0 ]]; then
        GIT_REF+="-${GIT_SYMBOLIC_REF}"
    fi
    # Only add the result of git describe if it's not the same string a $VERSION_NAME
    if [[ "$VERSION_NAME" != "$GIT_REF" ]]; then
		DATE=$(date +%F)
		VERSION_NAME+=" (${GIT_REF} ${DATE})"
    fi
fi

declare -r TMP_VERSION_FILE="${TMPDIR}/version.xml"
declare -r VERSION_FILE="${COMPONENT_DIR}/res/values/version.xml"

cat <<EOF > ${TMP_VERSION_FILE}
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="version">${VERSION_NAME}</string>
</resources>
EOF

if ! cmp --silent "$TMP_VERSION_FILE" "$VERSION_FILE"; then
    echo "Version file outdated, installing new: $VERSION_NAME"
    mv "$TMP_VERSION_FILE" "$VERSION_FILE"
else
   echo "Version file up to date: $VERSION_NAME"
fi
