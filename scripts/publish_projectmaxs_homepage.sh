#!/bin/bash

. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh

cd $HOMEPAGE

../scripts/getAssets.sh -f assets.db

emacs --batch -l emacs.el -f org-publish-all
