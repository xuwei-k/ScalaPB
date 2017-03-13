#!/usr/bin/env sh
set -e

./sbt -Dscala.ext.dirs=$HOME/.sbt/0.13/java9-rt-ext ++2.10.6 compilerPlugin/publishLocal runtimeJVM/publishLocal createVersionFile \
    ++2.12.2 runtimeJVM/publishLocal grpcRuntime/publishLocal
cd e2e
../sbt -Dscala.ext.dirs=$HOME/.sbt/0.13/java9-rt-ext clean test
