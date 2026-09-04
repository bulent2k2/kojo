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

cp -va installer/* installerbuild/

# The committed launch4j kojo.exe has its classpath baked in. If it predates the
# lib/scala-en entry it would start Kojo with no Scala on the classpath at all,
# so leave it out of the zip; bin/kojo.cmd is the zip's Windows entry point, and
# official Windows builds come from install4j (whose classpath is up to date).
# Regenerate the exe from installer/winlauncher-for-zip.xml with launch4j to
# ship it again.
if ! grep -q "scala-en" installerbuild/bin/kojo.exe 2>/dev/null; then
  echo "[INFO] Leaving stale kojo.exe out of the zip; bin/kojo.cmd is the Windows entry point."
  rm -f installerbuild/bin/kojo.exe
fi
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
