#!/usr/bin/env bash
set -e

# Source the config files
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/functions.sh"

showUsage() {
	echoerr "usage: ${0##*/} [+-d] <releaseVersion> <nextVersion>"
}

while getopts :d OPT; do
	case $OPT in
		d|+d)
			set -x
			;;
		*)
			showUsage
			exit 1
	esac
done
shift $(( OPTIND - 1 ))
OPTIND=1

readonly releaseVersion=${1}
readonly nextVersion=${2}

if [[ -z $releaseVersion ]]; then
	showUsage
	exit 2
fi

if [[ -z $nextVersion ]]; then
	showUsage
	exit 3
fi

# update_version set's the version *and* increases the versionCode of
# the components. This ensures that users that are currently on a
# pre-release version will automaticlly receive updates if their
# pre-release version got released.
setMaxsVersions -r true "$releaseVersion"

declare -r MESSAGE="MAXS Release $releaseVersion"

git commit -a -m "${MESSAGE}"
git tag -s -u flo@geekplace.eu -m "${MESSAGE}" $releaseVersion

# Now, after the tag was created, set the next version.

setMaxsVersions -r snapshot "$nextVersion"

git commit -a -m "MAXS Pre-Release $nextVersion"



