#!/bin/bash

while getopts dr:s OPTION "$@"; do
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

if [[ $RELEASE == *-SNAPSHOT* ]]; then
	SNAPSHOT=true
else
	SNAPSHOT=false
fi

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../../scripts/setup.sh"

XMPP_TRANSPORT_DIR=${BASEDIR}/transport-xmpp

cd "${XMPP_TRANSPORT_DIR}"

OLD_ASMACK_SHA=$(ls build/hashes/asmack-*.sha256)

# aSmack has now a min sdk default of 8
#MIN_SDK=$(xml sel -t -v "//manifest/uses-sdk/@android:minSdkVersion" AndroidManifest.xml)
MIN_SDK=8

TMP_DIR=$(mktemp -d)
trap "rm -rf ${TMP_DIR}" EXIT
cd $TMP_DIR

if $SNAPSHOT; then
	ASMACK_RELEASES+="/SNAPSHOTS"
fi

wget ${ASMACK_RELEASES}/${RELEASE}/asmack-android-${MIN_SDK}-${RELEASE}.jar || exit 1
wget ${ASMACK_RELEASES}/${RELEASE}/asmack-android-${MIN_SDK}-${RELEASE}.jar.sig || exit 1

gpg --verify asmack-android-${MIN_SDK}-${RELEASE}.jar.sig || exit 1

sha256sum asmack-android-${MIN_SDK}-${RELEASE}.jar > asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256

mv asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256 ${XMPP_TRANSPORT_DIR}/build/hashes

cd "${XMPP_TRANSPORT_DIR}"

# clean the lib dir of asmack
rm -f libs/asmack-*
rm -f libs-sources/asmack-*

git rm $OLD_ASMACK_SHA
git add build/hashes/asmack-android-${MIN_SDK}-${RELEASE}.jar.sha256
git commit -m "transport-xmpp: Changed aSmack version to ${RELEASE}"

make asmack
