#!/usr/bin/env sh
set -e
sbt +publishLocal createVersionFile &&
cd e2e &&
sbt ++$TRAVIS_SCALA_VERSION clean test
