#!/usr/bin/env bash
java -version
set -x 
# Build Kojo
rm -rf dist
./sbt.sh clean test buildDist

# Create staging area
rm -rf installerbuild
mkdir -p installerbuild/lib
cd installer
scala cp-staging-jars.scala
cd ..

# Stage the stock Scala toolchain into lib/scala-en; the launcher boots on it.
# The Turkish-keyword toolchain is not packaged - it is fetched on demand.
./stage-scala-toolchains.sh installerbuild/lib

# The committed launch4j exe has its classpath baked in; warn if it predates
# the lib/scala-en entry, since such a build ships a launcher that cannot find Scala.
if ! grep -q "scala-en" installer/bin/kojo.exe 2>/dev/null; then
  echo "[WARNING] installer/bin/kojo.exe has no lib/scala-en classpath entry -" >&2
  echo "[WARNING] regenerate it from installer/winlauncher-for-zip.xml with launch4j." >&2
fi

cp -va installer/* installerbuild/
cd installerbuild
rm *.*
rm -rf Uninstaller
cp licenses/Kojo-license.txt .
cd ..
rm -rf Kojo-z
mv installerbuild Kojo-z
cd Kojo-z
ln -s ~/tools/winxp-jre-8u152/jre
cd ..
rm Kojo.zip
zip -r Kojo.zip Kojo-z/*
