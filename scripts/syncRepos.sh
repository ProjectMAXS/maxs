#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

set -e

while getopts dr: OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
	r)
	    ONLY_REPO=${OPTARG}
	    ;;
    esac
done

OLDIFS=$IFS
IFS=";"
while read URL SHA1 REPO; do
    # Skip repos if ONLY_REPO is set
    if [[ -n $ONLY_REPO ]] && [[ $REPO != $ONLY_REPO ]]; then
	continue
    fi

    REPODIR=${BASEDIR}/repos/${REPO}
    if [[ -d $REPODIR ]]; then
	cd $REPODIR
	CURRENT_SHA1=$(git rev-parse HEAD)
	if [[ $SHA1 == $CURRENT_SHA1 ]]; then
	    continue
	fi
	git fetch
	git checkout $SHA1
    else
	git clone $URL $REPODIR
	cd $REPODIR
	git checkout $SHA1
    fi
done <${BASEDIR}/repos/repos.db
IFS=$OLDIFS
