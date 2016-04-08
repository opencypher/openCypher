#!/bin/bash

COMMAND=$1
shift

case "$(basename $0)" in
    *.jar)
        JARFILE="$(cd $(dirname $0); pwd)/$(basename $0)"
        ;;
    launch.sh)
        VERSION="$(grep -m1 -A1 '<artifactId>grammar</artifactId>' $(dirname $0)/../../../pom.xml | grep version | cut -d\> -f2 | cut -d\< -f1)"
        JARFILE="$(cd $(dirname $0)/../../../; pwd)/target/grammar-${VERSION}.jar"
        if ! [[ -f "$JARFILE" ]]; then
            pushd "$(dirname $0)/../../.."
            if ! mvn clean package; then
                >&2 echo Build failed
                exit 1
            fi
            popd
            if ! [[ -f "$JARFILE" ]]; then
                >&2 echo Cannot find jarfile in expected location, configuration error.
                >&2 echo JARFILE=$JARFILE
                exit 1
            fi
        fi
        ;;
    *)
        >&2 echo Cannot locate jarfile
        exit 1
    ;;
esac

PARAMETERS=
while [[ $# -gt 1 ]]; do
    case "$1" in
        --*=*)
            PARAMETERS="$PARAMETERS -D$COMMAND.${1:2}"
        ;;
        -*=*)
            PARAMETERS="$PARAMETERS -D$COMMAND.${1:1}"
        ;;
        *)
        break
        ;;
    esac
    shift
done

java $PARAMETERS -jar $JARFILE $COMMAND $*

exit 0
