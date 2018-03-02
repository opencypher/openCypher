#!/usr/bin/env bash

echo "Creating target directories..."
mkdir -p ${RELEASE_VERSION}/legacy
mkdir -p ${RELEASE_VERSION}/docs
echo "Done!"

echo "Generating railroad diagrams... "
./tools/grammar/src/main/shell/launch.sh RailRoadDiagramPages -outputDir=${RELEASE_VERSION}/railroad cypher.xml
./tools/grammar/src/main/shell/launch.sh RailRoadDiagramPages --INCLUDE_LEGACY=true -outputDir=${RELEASE_VERSION}/legacy/railroad cypher.xml
echo "Done!"

echo "Generating EBNF grammars... "
./tools/grammar/src/main/shell/launch.sh ISO14977 cypher.xml > ${RELEASE_VERSION}/cypher.ebnf
./tools/grammar/src/main/shell/launch.sh ISO14977 --INCLUDE_LEGACY=true cypher.xml > ${RELEASE_VERSION}/legacy/cypher.ebnf
./tools/grammar/src/main/shell/launch.sh Antlr4 cypher.xml > ${RELEASE_VERSION}/Cypher.g4
./tools/grammar/src/main/shell/launch.sh Antlr4 --INCLUDE_LEGACY=true cypher.xml > ${RELEASE_VERSION}/legacy/Cypher.g4
echo "Done!"

echo "Installing asciidoctor-pdf to generate style guide..."
gem install --local asciidoctor-pdf --pre
echo "Done!"

echo "Generating style guide pdf..."
asciidoctor-pdf -a pdf-style=docs/build/style-guide.yml -a pdf-fontsdir=docs/build/fonts docs/style-guide.adoc -o ${RELEASE_VERSION}/docs/style-guide.pdf
echo "Done!"

echo "Building archives with TCK and grammar source files..."
zip ${RELEASE_VERSION}/grammar-${RELEASE_VERSION}.zip grammar/*.xml
zip ${RELEASE_VERSION}/tck-${RELEASE_VERSION}.zip tck/features/*.feature
echo "Done!"

echo "Uploading artifacts to S3..."
aws s3 sync --acl public-read M10 s3://artifacts.opencypher.org/M10
echo "Done!"
