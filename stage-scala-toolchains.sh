#!/usr/bin/env bash
# Stages both Scala toolchains into <arg1>/scala-en and <arg1>/scala-tr
# (default arg1: installerbuild/lib):
#   scala-en: stock scala-library/reflect/compiler (downloaded from Maven
#             Central on first use, cached in scala-en-jars/) plus the
#             pristine scalariform from scala-tr/en/
#   scala-tr: the Turkish-keyword jars committed under scala-tr/build/pack/lib
#             plus the patched scalariform from lib/
# The Kojo launcher picks one of the two at startup based on the user
# language (see net.kogics.kojo.lite.ScalaToolchain). Run from the repo root.
set -e

libdir=${1:-installerbuild/lib}
scalaVer=$(grep -o 'scalaVer = "[0-9.]*"' build.sbt | grep -o '[0-9.]*[0-9]')
cache=scala-en-jars/$scalaVer

mkdir -p "$cache"
for jar in scala-library scala-reflect scala-compiler; do
  if [ ! -f "$cache/$jar.jar" ]; then
    echo "Fetching stock $jar $scalaVer from Maven Central..."
    curl -fL -o "$cache/$jar.jar" \
      "https://repo1.maven.org/maven2/org/scala-lang/$jar/$scalaVer/$jar-$scalaVer.jar"
  fi
done

mkdir -p "$libdir/scala-en" "$libdir/scala-tr"
cp "$cache"/scala-library.jar "$cache"/scala-reflect.jar "$cache"/scala-compiler.jar "$libdir/scala-en/"
cp scala-tr/en/scalariform.jar "$libdir/scala-en/scalariform.jar"
cp scala-tr/build/pack/lib/scala-library.jar scala-tr/build/pack/lib/scala-reflect.jar \
   scala-tr/build/pack/lib/scala-compiler.jar "$libdir/scala-tr/"
cp lib/scalariform.jar "$libdir/scala-tr/scalariform.jar"
echo "Staged Scala toolchains under $libdir/scala-en and $libdir/scala-tr"
