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

TODAYS_SNAPSHOT_DIR=$(ls -d $ASMACK_DIR/releases/* |grep $(date "+%Y-%m-%d"))
TODAYS_ASMACK_JAR_FULL=$(ls ${TODAYS_SNAPSHOT_DIR}/*.jar)
TODAYS_ASMACK_SOURCE_FULL=$(ls ${TODAYS_SNAPSHOT_DIR}/*.zip)
TODAYS_ASMACK_JAR=$(basename ${TODAYS_ASMACK_JAR_FULL})
TODAYS_ASMACK_SOURCE=$(basename ${TODAYS_ASMACK_SOURCE_FULL})
cp ${TODAYS_ASMACK_JAR_FULL} $TRANSPORT_DIR/libs/${TODAYS_ASMACK_JAR}
cp ${TODAYS_ASMACK_SOURCE_FULL} $TRANSPORT_DIR/libs-sources/${TODAYS_ASMACK_SOURCE}
echo "src=../libs-sources/${TODAYS_ASMACK_SOURCE}" > $TRANSPORT_DIR/libs/${TODAYS_ASMACK_JAR}.properties

pushd . > /dev/null
cd libs
sha256sum ${TODAYS_ASMACK_JAR} > $TRANSPORT_DIR/build/hashes/${TODAYS_ASMACK_JAR}.sha256
popd > /dev/null
