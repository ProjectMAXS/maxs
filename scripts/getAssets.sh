#!/bin/bash

set -e

# defaults
CHECK_SHA=true
REFETCH=false
OUTDIR=
ASSETSFILE=

while getopts df:no:r OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	f)
	    ASSETSFILE="${OPTARG}"
	    ;;
	o)
	    OUTDIR="${OPTARG}"
	    ;;
	n)
	    CHECK_SHA=false
	    ;;
	r)
	    REFETCH=true
	    ;;
    esac
done

if [[ ! -f $ASSETSFILE ]]; then
    echo "Not a file: " $ASSETSFILE
    exit 1
fi

[[ -z $OUTDIR ]] && OUTDIR=$(dirname $ASSETSFILE)

if [[ ! -d $OUTDIR ]]; then
    echo "Not a directory: " $OUTDIR
    exit 1
fi

OUTDIR=$(readlink -f $OUTDIR)

maybeCheckHash() {
    if [ $SHA256 != 0 -a \
	$(sha256sum $FILE | cut -f 1 -d ' ') != $SHA256 ]; then
	echo "Error: sha256 for $FILE does not match!"
	echo "expected=$SHA256 actual=$(sha256sum $FILE | cut -f 1 -d ' ')"
	exit 1
    fi
}

OLDIFS=$IFS
IFS=";"
while read URL DIR SHA256; do
    TARGETDIR=${OUTDIR}/${DIR}
    FILE=$(basename $URL)
    cd "${TARGETDIR}"

    if $REFETCH && [ -f $FILE ]; then
	rm $FILE
    fi
    if [[ -f $FILE ]]; then
	if $CHECK_SHA; then
	    maybeCheckHash
	fi
	continue
    fi

    wget $URL
    if $CHECK_SHA; then
	maybeCheckHash
    fi
done < $ASSETSFILE
IFS=$OLDIFS
