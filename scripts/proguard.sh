#!/bin/bash

. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"

set -e

readonly PROJECT_PROPERTIES=project.properties
readonly PROGUARD_ENABLED_STRING="proguard.enabled=true"
readonly PROGUARD_CONFIG_STRING="proguard.config=proguard.cfg"

show_usage_exit() {
    echo "usage: `basename $0` [-d] -c <component> -p (enabled|disabled)"
    exit 1
}

proguard_enabled() {
    return $(grep $PROGUARD_ENABLED_STRING $PROJECT_PROPERTIES &> /dev/null)
}

while getopts c:dp: OPTION "$@"; do
    case $OPTION in
	c)
	    COMPONENT=${OPTARG}
	    ;;
	d)
	    set -x
	    ;;
	p)
	    if [[ "enabled" == $OPTARG ]]; then
		PROGUARD=true
	    elif [[ "disabled" == $OPTARG ]]; then
		PROGUARD=false
	    else
		show_usage_exit
	    fi
    esac
done

# Also allow to enable or disable proguard with PROGAURD_ENABLED
# environment variable
[[ -n $PROGUARD_ENABLED ]] && PROGUARD=$PROGUARD_ENABLED

if [[ -z "$COMPONENT" ]] || [[ -z "$PROGUARD" ]]; then
    show_usage_exit
fi

COMPONENT_DIR=${BASEDIR}/${COMPONENT}

if [[ ! -d $COMPONENT_DIR ]]; then
    echo "error: not a directory $COMPONENT_DIR"
    exit 1
elif [[ ! -f "${COMPONENT_DIR}/proguard.cfg" ]]; then
    # Silently abort if the component has no proguard.cfg
    echo "Component has now proguard.cfg. Aborting"
    exit
fi

cd "${COMPONENT_DIR}"

if $PROGUARD && ! proguard_enabled ; then
    echo "${PROGUARD_ENABLED_STRING}" >> $PROJECT_PROPERTIES
    echo "${PROGUARD_CONFIG_STRING}" >> $PROJECT_PROPERTIES
elif ! $PROGUARD && proguard_enabled ; then
    sed -i "/${PROGUARD_ENABLED_STRING}/d" $PROJECT_PROPERTIES
    sed -i "/${PROGUARD_CONFIG_STRING}/d" $PROJECT_PROPERTIES
fi
