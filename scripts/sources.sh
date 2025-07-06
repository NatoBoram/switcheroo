#!/bin/sh

rm --force --recursive sources
mkdir --parents sources

cd sources
jar --extract --file='../.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-54576d3639/1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2/minecraft-merged-54576d3639-1.21.5-net.fabricmc.yarn.1_21_5.1.21.5+build.1-v2-sources.jar'
cd ..

gradle spotlessApply
