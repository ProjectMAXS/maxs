#!/bin/bash
set -e

while getopts dm: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	m)
	    DEST_NAME=${OPTARG}
	    ;;
    esac
done

if [[ -z "$DEST_NAME" ]]; then
    echo "usage: `basename $0` [-d] -m <moduleName>"
    exit 1
fi

SRC_NAME=bluetooth
SRC_MODULE=module-${SRC_NAME}
DEST_MODULE=module-${DEST_NAME}

if [[ $(basename $0) == createNewContribModule.sh ]]; then
    # setup.sh is found in the maxs submodule, in case we are called
    # as createNewContribModule.sh (i.e. from maxs-contrib)
    . "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../maxs/scripts/setup.sh"
    # If this script is called from maxs-contrib, then DEST_MODULE
    # should get created one directory below BASEDIR.
    DEST_MODULE=${BASEDIR}/../${DEST_MODULE}
else
    . "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"
fi

cd "${BASEDIR}"

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

cp -r -P $SRC_MODULE $DEST_MODULE
rm -rf ${DEST_MODULE}/bin/*
rm -rf ${DEST_MODULE}/gen/*
rm -rf ${DEST_MODULE}/Makefile
mv ${DEST_MODULE}/src/org/projectmaxs/module/${SRC_NAME} ${DEST_MODULE}/src/org/projectmaxs/module/${DEST_NAME}

# Copying relative symbolic links does not work, so we have to
# re-create those
rm ${DEST_MODULE}/res-src/drawable-mdpi/ic_maxs.svg
ln -rs ${BASEDIR}/main/res-src/drawable-mdpi/ic_maxs.svg ${DEST_MODULE}/res-src/drawable-mdpi/

# substitute the module name for the new one
find $DEST_MODULE -type f | xargs sed -i "s/${SRC_NAME}/${DEST_NAME}/"

# recreate the Makefile, eclipse links and the shared links in the source module
make ${SRC_MODULE}/Makefile
make -C $SRC_MODULE eclipse


cd $DEST_MODULE/..
git add ${DEST_MODULE}

make $(basename ${DEST_MODULE})/Makefile
make -C $DEST_MODULE eclipse

