#!/bin/bash

rm -f fluidanimate-2.log
rm -rf /tmp/parsec
mkdir /tmp/parsec

${1}/pkgs/apps/fluidanimate/inst/arm-linux.gcc/bin/fluidanimate 1 500  ${1}/pkgs/apps/fluidanimate/inputs/in_500K.fluid /tmp/parsec/out.fluid &>fluidanimate-2.log

ps -ef | grep inst/ | grep -v grep | awk '{print $2}' | xargs kill -9 &>/dev/null

exit 0