#!/usr/bin/env bash

echo "Build for Scala 2.12"
mvn -U clean install -P scala-212

echo "Build for Scala 2.13"
mvn -U clean install -P scala-213
