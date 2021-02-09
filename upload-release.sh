#!/usr/bin/env bash

#
# This script builds all artifacts necessary for an openCypher artifacts release, including
#   - railroad diagrams
#   - ANTLR grammar
#   - ISO14977 grammar
#   - Cypher Style Guide (requires Ruby)
#   - TCK as a zip archive
#   - XML Grammar source as a zip archive
#
# Additionally, this will synchronise the produced artifacts to the expected S3 bucket.
# (This step requires privileges to AWS.)
#
# NOTE: Set the environment variable RELEASE_VERSION to the appropriate value (e.g. M10)
#

# Fail if something fails
set -e

echo "Building release artifacts for ${RELEASE_VERSION} ... "

echo "Creating target directories..."
mkdir -p ${RELEASE_VERSION}/legacy
mkdir -p ${RELEASE_VERSION}/docs
echo "Done!"

echo "Generating railroad diagrams... "
./tools/grammar/src/main/shell/launch.sh RailRoadDiagramPages --user.country=US --user.language=en -outputDir=${RELEASE_VERSION}/railroad cypher.xml
./tools/grammar/src/main/shell/launch.sh RailRoadDiagramPages --INCLUDE_LEGACY=true --user.country=US --user.language=en -outputDir=${RELEASE_VERSION}/legacy/railroad cypher.xml
echo "Done!"

echo "Generating EBNF grammars... "
./tools/grammar/src/main/shell/launch.sh ISO14977 cypher.xml > ${RELEASE_VERSION}/cypher.ebnf
./tools/grammar/src/main/shell/launch.sh ISO14977 --INCLUDE_LEGACY=true cypher.xml > ${RELEASE_VERSION}/legacy/cypher.ebnf
./tools/grammar/src/main/shell/launch.sh Antlr4 cypher.xml > ${RELEASE_VERSION}/Cypher.g4
./tools/grammar/src/main/shell/launch.sh Antlr4 --INCLUDE_LEGACY=true cypher.xml > ${RELEASE_VERSION}/legacy/Cypher.g4
echo "Done!"

echo "Installing asciidoctor-pdf to generate style guide..."
gem install asciidoctor-pdf --pre
echo "Done!"

echo "Generating style guide pdf..."
asciidoctor-pdf -a pdf-style=docs/build/style-guide.yml -a pdf-fontsdir=docs/build/fonts docs/style-guide.adoc -o ${RELEASE_VERSION}/docs/style-guide.pdf
echo "Done!"

echo "Building archives with TCK and grammar source files..."
zip ${RELEASE_VERSION}/grammar-${RELEASE_VERSION}.zip grammar/*.xml
zip -r ${RELEASE_VERSION}/tck-${RELEASE_VERSION}.zip tck/features/* tck/index.adoc
echo "Done!"

echo "Artifacts all built!"

echo "Uploading artifacts to S3..."
aws s3 sync --acl public-read ${RELEASE_VERSION} s3://artifacts.opencypher.org/${RELEASE_VERSION}
echo "Done!"
