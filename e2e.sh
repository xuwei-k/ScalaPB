#!/usr/bin/env sh
set -e
sbt ++$TRAVIS_SCALA_VERSION createVersionFile
cd e2e
sbt ++$TRAVIS_SCALA_VERSION clean test:compile

