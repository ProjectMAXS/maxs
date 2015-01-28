#!/usr/bin/env bash
set -e

NIGHTLIES_URL=http://projectmaxs.org/releases/nightlies

FILTER="module-phonestatemodify"
DRY_RUN=false

while getopts b:df:ht OPTION "$@"; do
	case $OPTION in
		b)
			NIGHTLIES_URL+=/${OPTARG}/
			;;
		d)
			set -x
			;;
		f)
			FILTER=$OPTARG
			;;
		h)
			echo "usage: `basename $0` [-b <branch>] [-d] [-f <filter>] [-t]"
            echo " -b  optional branch"
			echo " -d  enable debug output"
            echo " -f  filter components from getting installed"
			echo " -t  dry-run, just print what would get installed"
			exit 1
			;;
		t)
			DRY_RUN=true
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
	if ! $DRY_RUN; then
		adb install -r $apk
	fi
done
