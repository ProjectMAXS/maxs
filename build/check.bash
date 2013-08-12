#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
MAXS_DIR="${SCRIPT_DIR}/.."

cd $MAXS_DIR

ack waitForDebugger
