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
    if [ -d ${c}/documentation ]; then
	ln -rs ${c}/documentation $(basename ${c})
    fi
done
