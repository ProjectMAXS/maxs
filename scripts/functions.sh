#!/usr/bin/env bash

# From https://stackoverflow.com/a/2990533/194894
echoerr() { printf "%s\n" "$*" >&2; }

get_package() {
    local manifest=${1}/AndroidManifest.xml
    local pkg
    pkg="$(xmlstarlet sel -t -v "//manifest/@package" "${manifest}")"
    echo "$pkg"
}

generateMaxsVersionCode() {
	set -e
	local isRelease="false"

	while getopts :dr: OPT; do
		case $OPT in
			d|+d)
				set -x
				;;
			r|+r)
				isRelease="$OPTARG"
				;;
			*)
				echo "usage: ${0##*/} [+-d] [+-r <true|false>] [--] <versionName>."
				exit 2
		esac
	done
	shift $(( OPTIND - 1 ))
	OPTIND=1

	declare -r versionName="${1}"
	if [[ -z "${versionName}" ]]; then
		echoerr "No version name provided, aborting."
		exit 1
	fi

	# Android's versionCode maximum value is INT32T_MAX: 2147483647
	# Which we split as follows:
	# 2 14 74 83657
	# |  |  |   |
	# |  |  |   - 2 x Days since 1.1.2016.
	# |  |  ----- Minor Version (max: 74 iff major==14, 99 otherwhise)
	# |  -------- Major Version (max: 14)
	# ----------- Constant value '2'

	# "Days since" allows for ~ 114 years (= 83657/(2*366)) if Major
	# Version ever reaches 14. Otherwhise ~ 136 years (=
	# 99999/(2*366)). This split means that versionName must exists
	# exaclty of a Major version and a Minor version. And Major
	# version MUST NOT be greater than 14.

	# Ideally I would have split as follows:
	# 21 47 4 83657
	# |  |  |   |
	# |  |  |   - Days since 1.1.2016. Allows for ~ 136 years (= 99999/(2*366))
	# |  |  ----- Patch Version
	# |  -------- Minor Version
	# ----------- Major Version

	# But since MAXS did use the epoch seconds as version code, which
	# are ~140000000, this split was not possible

	# Note that we double the days per year to allow for release
	# version codes, which have the same calculation but add one to
	# the days since.

	declare -i currentYear
	currentYear="$(date +%Y)"
	declare -i currentDay
	currentDay="$(date +%j)"

	# TODO implement caching functionality here

	IFS='.' read -ra versionComponents <<< "$versionName"

	declare -ir majorVersion=${versionComponents[0]}
	declare -ir minorVersion=${versionComponents[1]}

	declare -r numberRegex='^[0-9]+$'
	if ! [[ $majorVersion =~ $numberRegex ]]; then
		echo "Major version is not a number"
		exit 1
	fi
	if ! [[ $minorVersion =~ $numberRegex ]]; then
		echo "Minor version is not a number"
		exit 1
	fi

	if [[ $majorVersion -gt 14 ]]; then
		echo "Major version MUST NOT be greater than 14"
		exit 1;
	fi
	if [[ $majorVersion == 14 && $minorVersion -gt 74 ]]; then
		echo "Minor version MUST NOT be greater han 74 if Major version is 14"
		exit 1;
	fi

	declare -ir yearsSince2016=$((currentYear - 2016))

	declare -i dayCount=$(((yearsSince2016 * (2 * 366)) + (2 * currentDay)))

	case $isRelease in
		snapshot)
			((dayCount+=2))
			;;
		true)
			((dayCount++))
			;;
	esac

	declare -i versionCode=2000000000
	versionCode=$((versionCode + (majorVersion * 10000000)))
	versionCode=$((versionCode + (minorVersion * 100000)))
	versionCode=$((versionCode + dayCount))

	echo $versionCode
}

# Set the MAXS version code and optionally the version name (if given
# as second argument)
setMaxsVersion() {
	local isRelease="false"

	while getopts :dr: OPT; do
		case $OPT in
			d|+d)
				set -x
				;;
			r|+r)
				isRelease="$OPTARG"
				;;
			*)
				echo "usage: ${0##*/} [+-d] [+-r <true|false>] [--] <componentDirectory> [<versionName>]."
				exit 2
		esac
	done
	shift $(( OPTIND - 1 ))
	OPTIND=1

	declare -r componentDirectory="$1"
	declare -r manifest="${componentDirectory}/AndroidManifest.xml"
	if [[ $# -gt 1 ]]; then
		declare -r setVersionName="true"
		local versionName="$2"
	else
		declare -r setVersionName="false"
		local versionName
		versionName="$(xmlstarlet sel -t -v "//manifest/@android:versionName" "${manifest}")"
	fi

	local versionCode
	versionCode=$(generateMaxsVersionCode -r "$isRelease" "$versionName")

    # Sadly, this also modifies the layout of the
    # AndroidManifest. Would be cool to use xmlstarlet for XML
    # modifications
#    xml ed -P -S -u "//manifest/@android:versionCode" -v $newVersionCode $manifest
#    xml ed -P -S -u "//manifest/@android:versionName" -v $newVersionName $manifest

	sed -i "s/android:versionCode=\"[^\"]*\"/android:versionCode=\"${versionCode}\"/" "${manifest}"

	if $setVersionName; then
		sed -i "s/android:versionName=\"[^\"]*\"/android:versionName=\"${versionName}\"/" "${manifest}"
	fi
}

setMaxsVersions() {
	local isRelease="false"

	while getopts :dr: OPT; do
		case $OPT in
			d|+d)
				set -x
				;;
			r|+r)
				isRelease="$OPTARG"
				;;
			*)
				echo "usage: ${0##*/} [+-d] [+-r <true|false>] [--] <versionName>."
				exit 2
		esac
	done
	shift $(( OPTIND - 1 ))
	OPTIND=1

	local -r maxsVersion="$1"

	setMaxsVersion -r "$isRelease" "$MAINDIR" "$maxsVersion"
	for t in $TRANSPORTS ; do
		setMaxsVersion  -r "$isRelease" "$t" "$maxsVersion"
	done
	for m in $MODULES ; do
		setMaxsVersion -r "$isRelease" "$m" "$maxsVersion"
	done
}
