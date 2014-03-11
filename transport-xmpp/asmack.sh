#!/bin/bash
set -e

[[ $1 == "-d" ]] && set -x

# config
ASMACK_RELEASES=http://asmack.freakempire.de/

ASMACK_JAR_SHA256=$(cd libs && ls ../build/hashes/asmack-android-*.jar.sha256)
# TODO check that only one asmack sha256 file is found

# aSmack has now a min sdk default of 8
#MIN_SDK_VERSION=$(grep minSdkVersion AndroidManifest.xml | awk -F"\"" '{print $2}')
MIN_SDK_VERSION=8

MIN_SDK_VERSION_SHA_FILE=$( echo $ASMACK_JAR_SHA256 |  awk -F- '{print $3}')
# TODO check that both min sdk versions are in sync

ASMACK_VER=$(echo $ASMACK_JAR_SHA256 | cut -d- -f4-)
ASMACK_VER=${ASMACK_VER/%.jar.sha256/}

if [[ $ASMACK_VER == *-SNAPSHOT* ]]; then
	ASMACK_RELEASES+="/SNAPSHOTS"
fi

ASMACK_JAR=asmack-android-${MIN_SDK_VERSION}-${ASMACK_VER}.jar
ASMACK_SRC=asmack-android-${MIN_SDK_VERSION}-source-${ASMACK_VER}.zip

ASMACK_JAR_URL=${ASMACK_RELEASES}/${ASMACK_VER}/${ASMACK_JAR}
ASMACK_SRC_URL=${ASMACK_RELEASES}/${ASMACK_VER}/${ASMACK_SRC}

pushd . > /dev/null
cd libs
# Delete old asmack jars
TO_RM=$(find . -type f \( -name 'asmack-android-*.jar' -a ! -name $ASMACK_JAR \))
[[ -n $TO_RM ]] && rm $TO_RM

DONE=false
if [[ ! -f $ASMACK_JAR ]]; then
	wget ${ASMACK_JAR_URL} 2>&1 || exit 1
fi
sha256sum -c ${ASMACK_JAR_SHA256} || exit 1

# Create the properties file for the container lib. Allows convinitent
# browsing of the aSmack source in Eclipse. Thanks to
# http://stackoverflow.com/a/12639812/194894
if [[ ! -f $ASMACK_JAR.properties ]]; then
    echo "src=../libs-sources/${ASMACK_SRC}" > $ASMACK_JAR.properties
fi
popd > /dev/null

pushd . > /dev/null
cd libs-sources
# Delete old asmack source zips
TO_RM=$(find . -type f \( -name 'asmack-android-*.zip' -a ! -name $ASMACK_SRC \))
[[ -n $TO_RM ]] && rm $TO_RM

if [[ ! -f $ASMACK_SRC ]]; then
    wget ${ASMACK_SRC_URL} 2>&1 || exit 1
    wget ${ASMACK_SRC_URL}.md5 2>&1 || exit 1
    md5sum -c ${ASMACK_SRC}.md5 || exit 1
    rm ${ASMACK_SRC}.md5
fi
popd > /dev/null

sed -i \
    -e "s/asmack-android-.*jar/${ASMACK_JAR}/" \
    -e "s/sources\/asmack-android-.*zip/sources\/${ASMACK_SRC}/" \
    build/eclipse/classpath
