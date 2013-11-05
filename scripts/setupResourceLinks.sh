#!/bin/bash

set -e

while getopts c:d OPTION "$@"; do
    case $OPTION in
	c)
	    COMPONENT=${OPTARG}
	    ;;
	d)
	    set -x
	    ;;
    esac
done

if [[ -z "$COMPONENT" ]]; then
    echo "usage: `basename $0` [-d] -c <component>"
    exit 1
fi

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

IS_MAIN=false
IS_MODULE=false
IS_TRANSPORT=false

[[ $COMPONENT == main ]] && IS_MAIN=true
[[ $COMPONENT == module-* ]] && IS_MODULE=true
[[ $COMPONETN == transport-* ]] && IS_TRANSPORT=true

cd $BASEDIR/$COMPONENT/res/

# Phase 1: The global shared resources

for valueDir in $(find ${BASEDIR}/shared/res-values-global/ -mindepth 1 -maxdepth 1 -type d); do
    # Make sure that the directory exists
    [[ ! -d $valueDir ]] && mkdir $valueDir
    for resFile in ${valueDir}/*; do
	TARGET=$(basename $valueDir)/$(basename $resFile)
	# Symlink is already in place
	[[ -h $TARGET ]] && continue;
	ln -rs $resFile $TARGET
    done
done
