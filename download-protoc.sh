#!/bin/bash
set -e
set -x
ZIP_NAME=protoc-3.0.0-linux-x86_64.zip
wget https://github.com/google/protobuf/releases/download/v3.0.0/${ZIP_NAME}
unzip ${ZIP_NAME}
chmod +x bin/protoc
