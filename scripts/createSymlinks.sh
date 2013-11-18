#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

while getopts d OPTION "$@"; do
    case $OPTION in
	d)
	    set -x
	    ;;
    esac
done

cd $DOCDIR

for c in $COMPONENTS; do
    BASENAME_COMPONENT=$(basename ${c})
    if [ -d ${c}/documentation ] && [ ! -L $BASENAME_COMPONENT ]; then
	ln -rs ${c}/documentation $BASENAME_COMPONENT
    fi
done
