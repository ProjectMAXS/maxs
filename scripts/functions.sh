#!/bin/bash

get_package() {
    local manifest=${1}/AndroidManifest.xml
    local pkg=$(xml sel -t -v "//manifest/@package" ${manifest})
    echo $pkg
}

update_version() {
    local dir=${1}
    local newVersionName=${2}

    local manifest=${dir}/AndroidManifest.xml
    local versionCode=$(xml sel -t -v "//manifest/@android:versionCode" ${manifest})
    local newVersionCode=$(($versionCode + 1))

    # Sadly, this also modifies the layout of the
    # AndroidManifest. Would be cool to use xmlstarlet for XML
    # modifications
#    xml ed -P -S -u "//manifest/@android:versionCode" -v $newVersionCode $manifest
#    xml ed -P -S -u "//manifest/@android:versionName" -v $newVersionName $manifest

    sed -i "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"${newVersionCode}\"/" ${manifest}
    sed -i "s/android:versionName=\"[^\"]*\"/android:versionName=\"${newVersionName}\"/" ${manifest}
}

set_version() {
    local dir=${1}
    local newVersionName=${2}

    local manifest=${dir}/AndroidManifest.xml
    sed -i "s/android:versionName=\"[^\"]*\"/android:versionName=\"${newVersionName}\"/" ${manifest}
}
