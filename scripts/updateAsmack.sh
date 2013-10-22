#!/bin/bash

while getopts dr: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	r)
	    RELEASE=${OPTARG}
	    ;;
	esac
done

# config
ASMACK_RELEASES=http://asmack.freakempire.de/

if [[ -z $RELEASE ]]; then
    echo "usage: `basename $0` [-d] -r <aSmackRelease>"
    exit 1
fi

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

XMPP_TRANSPORT_DIR=${BASEDIR}/transport-xmpp

cd "${XMPP_TRANSPORT_DIR}"

OLD_ASMACK_SHA=$(ls build/hashes/asmack-*.sha256)
MIN_SDK=$(xml sel -t -v "//manifest/uses-sdk/@android:minSdkVersion" AndroidManifest.xml)

TMP_DIR=$(mktemp -d)
trap "rm -rf ${TMP_DIR}" EXIT
cd $TMP_DIR

wget ${ASMACK_RELEASES}/${RELEASE}/asmack-android-${MIN_SDK}-${RELEASE}.jar || exit 1
wget ${ASMACK_RELEASES}/${RELEASE}/asmack-android-${MIN_SDK}-${RELEASE}.jar.sig || exit 1

gpg --verify asmack-android-${MIN_SDK}-${RELEASE}.jar.sig || exit 1

sha256sum asmack-android-${MIN_SDK}-${RELEASE}.jar > asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256

mv asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256 ${XMPP_TRANSPORT_DIR}/build/hashes

cd "${XMPP_TRANSPORT_DIR}"

# clean the lib dir of asmack
rm libs/asmack-*
rm libs-sources/asmack-*

git rm $OLD_ASMACK_SHA
git add build/hashes/asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256
git commit -m "Changed aSmack version to ${RELEASE}"

make asmack
