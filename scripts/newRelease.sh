#!/bin/bash

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
update_version $MAINDIR $releaseVersion
for t in $TRANSPORTS ; do
    update_version $t $releaseVersion
done
for m in $MODULES ; do
    update_version $m $releaseVersion
done

if git diff --exit-code ; then
    echo "No unstaged changes. Not creating a release commit"
else
    git commit -a -m "MAXS Release $releaseVersion"
fi
git tag -s -u flo@geekplace.eu -m "MAXS Release $releaseVersion" $releaseVersion

update_version $MAINDIR $nextVersion
for t in $TRANSPORTS ; do
    update_version $t $nextVersion
done
for m in $MODULES ; do
    update_version $m $nextVersion
done

git commit -a -m "MAXS Pre-Release $nextVersion"



