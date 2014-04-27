#!/bin/bash
set -e

while getopts d OPTION "$@"; do
	case $OPTION in
		d)
			set -x
			;;
	esac
done

# Pretty fancy method to get reliable the absolute path of a shell
# script, *even if it is sourced*. Credits go to GreenFox on
# stackoverflow: http://stackoverflow.com/a/12197518/194894
pushd . > /dev/null
SCRIPTDIR="${BASH_SOURCE[0]}";
while([ -h "${SCRIPTDIR}" ]); do
    cd "`dirname "${SCRIPTDIR}"`"
    SCRIPTDIR="$(readlink "`basename "${SCRIPTDIR}"`")";
done
cd "`dirname "${SCRIPTDIR}"`" > /dev/null
SCRIPTDIR="`pwd`";
popd  > /dev/null

BASEDIR="$(readlink -f $SCRIPTDIR/../..)"
TRANSPORT_DIR="$(readlink -f $SCRIPTDIR/..)"

if [[ -f ${BASEDIR}/config ]]; then
	   # config is there, source it
    . ${BASEDIR}/config
else
	echo "Config file not found"
	exit 1
fi

if [[ -z $ASMACK_DIR ]]; then
	echo "ASMACK_DIR not set in config file"
	exit 1
fi

pushd . > /dev/null
cd $ASMACK_DIR
./build.bash $@
popd > /dev/null

rm -f $TRANSPORT_DIR/build/hashes/asmack-android*
rm -f $TRANSPORT_DIR/libs/asmack-android*
rm -f $TRANSPORT_DIR/libs-sources/asmack-android*

cp $ASMACK_DIR/build/asmack-android-8.jar $TRANSPORT_DIR/libs/asmack-android-8-9999.jar
cp $ASMACK_DIR/build/asmack-android-8-source.zip $TRANSPORT_DIR/libs-sources/asmack-android-8-source-9999.zip
echo "src=../libs-sources/asmack-android-8-source-9999.zip" > $TRANSPORT_DIR/libs/asmack-android-8-9999.jar.properties

pushd . > /dev/null
cd libs
sha256sum asmack-android-8-9999.jar > $TRANSPORT_DIR/build/hashes/asmack-android-8-9999.jar.sha256
popd > /dev/null
