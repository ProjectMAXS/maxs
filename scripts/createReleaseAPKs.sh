#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

set -e

PUBLISH=false

while getopts dpt: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	p)
	    PUBLISH=true
	    ;;
	t)
	    RELEASE_TAG="${OPTARG}"
	    ;;
    esac
done

if $PUBLISH && [[ -z $RELEASE_TAG ]]; then
    echo "error: publishing (-p), but no release tag specified (-t)"
    exit 1
fi

KEYSTOREFILE=${KEYSTOREDATA}/release.keystore
KEYSTOREPASSGPG=${KEYSTOREDATA}/keystore_password.gpg

if [[ ! -d ${KEYSTOREDATA} ]]; then
    echo "${KEYSTOREDATA} does not exist or is not a directory"
    exit 1
fi

if [[ ! -f ${KEYSTOREFILE} ]]; then
    echo "${KEYSTOREFILE} does not exist or is not a file"
    exit 1
fi

if [[ ! -f ${KEYSTOREPASSGPG} ]]; then
    echo "${KEYSTOREPASSGPG} does not exist or is not a file"
    exit 1
fi

TMPDIR=$(mktemp -d)
trap "rm -rf ${TMPDIR}" EXIT

KEYSTOREPASS=$(cat ${KEYSTOREPASSGPG} | gpg -d)

cat <<EOF > ${TMPDIR}/ant.properties
key.store=${KEYSTOREFILE}
key.alias=maxs
key.store.password=${KEYSTOREPASS}
key.alias.password=${KEYSTOREPASS}
EOF

cd ${BASEDIR}

if [[ -n $RELEASE_TAG ]]; then
    git checkout $RELEASE_TAG
fi

ANT_ARGS="-propertyfile ${TMPDIR}/ant.properties" make parrelease

# It doesn't matter that $RELEASE_TAG may be empty in case we havn't
# defined a release tag. It's still a good idea to copy the apks to
# one location.
[[ -d releases/${RELEASE_TAG} ]] || mkdir -p releases/${RELEASE_TAG}

for c in $COMPONENTS; do
    cp ${c}/bin/*-release.apk releases/${RELEASE_TAG}
done

if [[ -n $RELEASE_TAG ]]; then
    git checkout master
fi

if $PUBLISH; then
    cat <<EOF | sftp $RELEASE_HOST
mkdir ${RELEASE_DIR}/${RELEASE_TAG}
put releases/${RELEASE_TAG}/* ${RELEASE_DIR}/${RELEASE_TAG}
EOF
fi
