#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

cd "${BASEDIR}"

set -e

SRC_NAME=bluetooth
SRC_MODULE=module-${SRC_NAME}
DEST_NAME=$1
DEST_MODULE=module-${DEST_NAME}

if [ -d $DEST_MODULE ]; then
    echo $DEST_MODULE already exists
    exit 1
fi

if [[ $DEST_NAME =~ .*-.* ]]; then
    echo Module name must not contain '-'
    exit 1
fi

# First clean the source module
pushd .
cd $SRC_MODULE
git clean -x -f -d 
popd

cp -r $SRC_MODULE $DEST_MODULE
rm -rf ${DEST_MODULE}/bin/*
rm -rf ${DEST_MODULE}/gen/*
rm -rf ${DEST_MODULE}/Makefile
mv ${DEST_MODULE}/src/org/projectmaxs/module/${SRC_NAME} ${DEST_MODULE}/src/org/projectmaxs/module/${DEST_NAME}

# substitute the module name for the new one
find $DEST_MODULE -type f | xargs sed -i "s/${SRC_NAME}/${DEST_NAME}/"

git add ${DEST_MODULE}

make ${DEST_MODULE}/Makefile
make -C $DEST_MODULE eclipse

# recreate the Makefile, eclipse links and the shared links in the source module
make ${SRC_MODULE}/Makefile
make -C $SRC_MODULE eclipse
