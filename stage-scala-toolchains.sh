#!/usr/bin/env bash
# Stages the stock Scala toolchain into <arg1>/scala-en (default arg1:
# installerbuild/lib): scala-library/reflect/compiler downloaded from Maven
# Central at build.sbt's scalaVer, plus scalariform from lib/.
#
# The launcher boots on this directory and, for a Turkish user, swaps in a
# lib/scala-tr with the Turkish-keyword compiler - which is not packaged here
# and is fetched on demand instead; see net.kogics.kojo.lite.ScalaToolchain.
#
# Runs from the repo root regardless of the caller's cwd; a relative <arg1>
# resolves against the repo root.
set -euo pipefail
cd "$(dirname "$0")"

libdir=${1:-installerbuild/lib}
scalaVer=$(sed -n 's/^lazy val scalaVer *= *"\([0-9][0-9.]*\)".*/\1/p' build.sbt)
if [ -z "$scalaVer" ]; then
  echo "Could not read scalaVer from build.sbt" >&2
  exit 1
fi
cache=scala-en-jars/$scalaVer

mkdir -p "$cache"
for jar in scala-library scala-reflect scala-compiler; do
  if [ ! -f "$cache/$jar.jar" ]; then
    echo "Fetching $jar $scalaVer from Maven Central..."
    curl -fL --retry 3 --retry-delay 2 -o "$cache/$jar.jar.tmp" \
      "https://repo1.maven.org/maven2/org/scala-lang/$jar/$scalaVer/$jar-$scalaVer.jar"
    unzip -tqq "$cache/$jar.jar.tmp" # integrity check: a truncated download fails here, not at runtime
    mv "$cache/$jar.jar.tmp" "$cache/$jar.jar"
  fi
done

mkdir -p "$libdir/scala-en"
cp "$cache"/scala-library.jar "$cache"/scala-reflect.jar "$cache"/scala-compiler.jar "$libdir/scala-en/"
cp lib/scalariform.jar "$libdir/scala-en/scalariform.jar"
echo "Staged the Scala $scalaVer toolchain under $libdir/scala-en"
