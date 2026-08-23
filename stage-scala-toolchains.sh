#!/usr/bin/env bash
# Stages both Scala toolchains into <arg1>/scala-en and <arg1>/scala-tr
# (default arg1: installerbuild/lib):
#   scala-en: stock scala-library/reflect/compiler at the same Scala version
#             the patched jars were built from (read from the patched
#             scala-library.jar; downloaded from Maven Central on first use,
#             cached in scala-en-jars/) plus the pristine scalariform from
#             scala-tr/en/
#   scala-tr: the Turkish-keyword jars committed under scala-tr/build/pack/lib
#             plus the patched scalariform from lib/
# The Kojo launcher picks one of the two at startup based on the user
# language (see net.kogics.kojo.lite.ScalaToolchain).
# Runs from the repo root regardless of the caller's cwd; a relative <arg1>
# resolves against the repo root.
set -euo pipefail
cd "$(dirname "$0")"

libdir=${1:-installerbuild/lib}
# Match the stock version to the patched build, so the two toolchains differ
# only in the Turkish-keyword patches.
scalaVer=$(unzip -p scala-tr/build/pack/lib/scala-library.jar library.properties |
  sed -n 's/^version\.number=\([0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*\).*/\1/p')
if [ -z "$scalaVer" ]; then
  echo "Could not read the Scala version from scala-tr/build/pack/lib/scala-library.jar" >&2
  exit 1
fi
cache=scala-en-jars/$scalaVer

mkdir -p "$cache"
for jar in scala-library scala-reflect scala-compiler; do
  if [ ! -f "$cache/$jar.jar" ]; then
    echo "Fetching stock $jar $scalaVer from Maven Central..."
    curl -fL --retry 3 --retry-delay 2 -o "$cache/$jar.jar.tmp" \
      "https://repo1.maven.org/maven2/org/scala-lang/$jar/$scalaVer/$jar-$scalaVer.jar"
    unzip -tqq "$cache/$jar.jar.tmp" # integrity check: a truncated download fails here, not at runtime
    mv "$cache/$jar.jar.tmp" "$cache/$jar.jar"
  fi
done

mkdir -p "$libdir/scala-en" "$libdir/scala-tr"
cp "$cache"/scala-library.jar "$cache"/scala-reflect.jar "$cache"/scala-compiler.jar "$libdir/scala-en/"
cp scala-tr/en/scalariform.jar "$libdir/scala-en/scalariform.jar"
cp scala-tr/build/pack/lib/scala-library.jar scala-tr/build/pack/lib/scala-reflect.jar \
   scala-tr/build/pack/lib/scala-compiler.jar "$libdir/scala-tr/"
cp lib/scalariform.jar "$libdir/scala-tr/scalariform.jar"
echo "Staged Scala toolchains ($scalaVer stock + Turkish keywords) under $libdir/scala-en and $libdir/scala-tr"
