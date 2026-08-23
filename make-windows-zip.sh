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

# Stage both Scala toolchains (stock + Turkish keywords); the launcher
# picks one at startup based on the user language.
./stage-scala-toolchains.sh installerbuild/lib

# The zip ships the committed launch4j exe; it must embed the lib/scala-en
# classpath entry (regenerate it with launch4j from
# installer/winlauncher-for-zip.xml after any launcher classpath change).
if ! grep -aq 'scala-en' installer/bin/kojo.exe; then
  echo '[WARNING] installer/bin/kojo.exe is stale: it does not reference lib/scala-en and will not start from this layout. Regenerate it with launch4j from installer/winlauncher-for-zip.xml before shipping.'
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
