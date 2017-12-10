#!/usr/bin/env bash

# Source the config files
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh"
. "$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/functions.sh"

if [[ $# -ne 2 ]]; then
    echo "usage: `basename $0` <releaseVersion> <nextVersion>"
    exit 1
fi

set -x
set -e

releaseVersion=${1}
nextVersion=${2}

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



