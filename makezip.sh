#!/usr/bin/env bash
java -version
set -x 
# Build Kojo
rm -rf dist
./sbt.sh clean test buildDist

echo '[info] Linking to jars of the Scala compiler with Turkish keywords'
tgt=dist
# Relative to dist:
src=../scala-tr/build/pack/lib
BU=$tgt/ORG
mkdir -p $BU
rm -if $BU/*.jar
mv $tgt/scala-compiler* $tgt/scala-library* $tgt/scala-reflect* $BU/
ln -s $src/scala-compiler.jar $tgt/scala-compiler.jar
ln -s $src/scala-library.jar  $tgt/scala-library.jar
ln -s $src/scala-reflect.jar  $tgt/scala-reflect.jar

# Create staging area
rm -rf installerbuild
mkdir -p installerbuild/lib
cd installer
scala cp-staging-jars.scala
cd ..

cp -va installer/* installerbuild/
cd installerbuild
rm *.*
rm -rf Uninstaller
cp licenses/Kojo-license.txt .
cd ..
rm -rf Kojo-z Kojo-z??
mv installerbuild Kojo-z
rm Kojo.zip Kojo??.zip
zip -r Kojo.zip Kojo-z/*
