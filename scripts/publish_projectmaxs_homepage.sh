#!/bin/bash

. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/setup.sh

cd $HOMEPAGE

emacs --batch -l emacs.el -f org-publish-all
