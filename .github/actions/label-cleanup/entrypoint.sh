#!/bin/sh

set -e
sh -c "gem install octokit"
sh -c "ruby /action.rb $*"
