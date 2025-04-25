#!/bin/sh

mkdir -p sources
cd sources
jar --extract --file='.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-bb64cf0275/1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2/minecraft-merged-bb64cf0275-1.21.4-net.fabricmc.yarn.1_21_4.1.21.4+build.8-v2-sources.jar'
