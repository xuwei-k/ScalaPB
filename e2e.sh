#!/usr/bin/env sh
set -e
rm -rf $HOME/.ivy2/cache/com.trueaccord.scalapb/ &&
sbt +publishLocal createVersionFile &&
cd e2e &&
sbt clean test

