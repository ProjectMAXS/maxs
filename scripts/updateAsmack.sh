#!/bin/bash

set -x
set -e

# config
ASMACK_RELEASES=http://asmack.freakempire.de/

if [[ $# -ne 1 ]]; then
    echo "usage: `basename $0` <aSmackRelease>"
    exit 1
fi

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

XMPP_TRANSPORT_DIR=${BASEDIR}/transport-xmpp

cd "${XMPP_TRANSPORT_DIR}"

OLD_ASMACK_SHA=$(ls build/hashes/asmack-*.sha256)
MIN_SDK=$(xml sel -t -v "//manifest/uses-sdk/@android:minSdkVersion" AndroidManifest.xml)

TMP_DIR=$(mktemp -d)
cd $TMP_DIR

wget ${ASMACK_RELEASES}/${1}/asmack-android-${MIN_SDK}-${1}.jar
wget ${ASMACK_RELEASES}/${1}/asmack-android-${MIN_SDK}-${1}.jar.sig

gpg --verify asmack-android-${MIN_SDK}-${1}.jar.sig

sha256sum asmack-android-${MIN_SDK}-${1}.jar > asmack-android-${MIN_SDK}-${1}.jar.sha256

mv asmack-android-${MIN_SDK}-${1}.jar.sha256 ${XMPP_TRANSPORT_DIR}/build/hashes

cd "${XMPP_TRANSPORT_DIR}"
rm -rf $TMP_DIR

git rm $OLD_ASMACK_SHA

rm libs/asmack-*
rm libs-sources/asmack-*

git add build/hashes/asmack-android-${MIN_SDK}-${1}.jar.sha256

