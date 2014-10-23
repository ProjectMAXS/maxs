#!/usr/bin/env bash
set -e

declare -r NIGHTLIES_URL=http://projectmaxs.org/releases/nightlies/

FILTER="module-phonestatemodify"

while getopts df: OPTION "$@"; do
	case $OPTION in
		d)
			set -x
			;;
		f)
			FILTER=$OPTARG
			;;
	esac
done

TMPDIR=$(mktemp -d)
trap "rm -rf ${TMPDIR}" EXIT

CURRENT_NIGHTLY_DIR=

while read line; do
	DIR=$(echo $line | awk -F '[<>]' '/<tr>.*/ { print $13 }')
	if [[ -z $DIR ]]; then
		continue
	elif [[ $DIR =~ ^[a-zA-Z]+ ]]; then
		continue
	elif [[ $DIR > $CURRENT_NIGHTLY_DIR ]]; then
		CURRENT_NIGHTLY_DIR=$DIR
	fi
done < <(curl $NIGHTLIES_URL)

cd $TMPDIR

echo "Downloading and installing MAXS Nightly from " $CURRENT_NIGHTLY_DIR

wget --no-parent -r -l1 -nH -nd -nv -A apk $NIGHTLIES_URL/$CURRENT_NIGHTLY_DIR

for apk in *.apk; do
	if [[ -n $FILTER ]]; then
		FILTER_ARRAY=(${FILTER/,/ })
		for f in $FILTER_ARRAY; do
			if [[ maxs-${f}-release.apk == $apk ]]; then
				continue 2
			fi
		done
	fi
	echo "Installing $apk"
	adb install -r $apk
done
