#!/bin/bash

. config

MIN_SDK_VERSION=$(grep minSdkVersion AndroidManifest.xml | awk -F"\"" '{print $2}')

ASMACK_JAR=asmack-android-${MIN_SDK_VERSION}-${ASMACK_VER}.jar
ASMACK_SRC=asmack-android-${MIN_SDK_VERSION}-source-${ASMACK_VER}.zip

ASMACK_JAR_URL=${ASMACK_RELEASES}/${ASMACK_VER}/${ASMACK_JAR}
ASMACK_SRC_URL=${ASMACK_RELEASES}/${ASMACK_VER}/${ASMACK_SRC}

pushd . > /dev/null
cd libs
if [[ ! -f $ASMACK_JAR ]]; then 
    wget ${ASMACK_JAR_URL}
    wget ${ASMACK_JAR_URL}.md5
    md5sum -c ${ASMACK_JAR}.md5 || exit 1
    rm ${ASMACK_JAR}.md5
fi
popd > /dev/null

pushd . > /dev/null
cd libs-sources
if [[ ! -f $ASMACK_SRC ]]; then
    wget ${ASMACK_SRC_URL}
    wget ${ASMACK_SRC_URL}.md5
    md5sum -c ${ASMACK_SRC}.md5 || exit 1
    rm ${ASMACK_SRC}.md5
fi
popd > /dev/null

sed -i \
    -e "s/asmack-android-.*jar/${ASMACK_JAR}/" \
    -e "s/sources\/asmack-android-.*zip/sources\/${ASMACK_SRC}/" \
    .classpath
