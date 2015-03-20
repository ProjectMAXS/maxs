#!/bin/bash

get_package() {
    local manifest=${1}/AndroidManifest.xml
    local pkg=$(xml sel -t -v "//manifest/@package" ${manifest})
    echo $pkg
}

set_versionCode() {
    local manifest=$1/AndroidManifest.xml
    local newVersionCode=$2

    if [ ! -f "${manifest}" ]; then
        echo "$manifest is not a file"
        exit 1
    fi

    sed -i "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"${newVersionCode}\"/" ${manifest}
}

update_version() {
    local dir=${1}
    local newVersionName=${2}

    local manifest=${dir}/AndroidManifest.xml
    local newVersionCode=$(date +%s)

    # Sadly, this also modifies the layout of the
    # AndroidManifest. Would be cool to use xmlstarlet for XML
    # modifications
#    xml ed -P -S -u "//manifest/@android:versionCode" -v $newVersionCode $manifest
#    xml ed -P -S -u "//manifest/@android:versionName" -v $newVersionName $manifest

    set_versionCode $dir $newVersionCode
    sed -i "s/android:versionName=\"[^\"]*\"/android:versionName=\"${newVersionName}\"/" ${manifest}
}
