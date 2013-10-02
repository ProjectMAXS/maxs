#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

if [[ -z $1 ]]; then
    echo "version not set"
    exit 1
fi


# Create version entry for the modules
for m in "${!MOD2PKG[@]}"; do
    versionCode=$(xml sel -t -v "//manifest/@android:versionCode" ${BASEDIR}/${m}/AndroidManifest.xml)
    cat <<EOF >> ${FDROIDMETA}/${MOD2PKG[${m}]}.txt

Build Version:${1},${versionCode},${1},\\
init=(cd .. && make ${m}/Makefile},\\
subdir=${m},\\
prebuild=make shared
EOF
exit
done

# Create version entry for main
versionCode=$(xml sel -t -v "//manifest/@android:versionCode" ${BASEDIR}/main/AndroidManifest.xml)
cat <<EOF >> ${FDROIDMETA}/org.projectmaxs.main.txt

Build Version:${1},${versionCode},${1},\\
subdir=main,\\
prebuild=make resources
EOF

# Create version entry for transport
versionCode=$(xml sel -t -v "//manifest/@android:versionCode" ${BASEDIR}/main/AndroidManifest.xml)
cat <<EOF >> ${FDROIDMETA}/org.projectmaxs.transport.xmpp.txt

Build Version:${1},${versionCode},${1},\\
subdir=transport-xmpp,\\
prebuild=make asmack shared
EOF
