#!/bin/sh

rm -rf .classpath .factorypath .gradle .project .settings .vscode/launch.json bin build run switcheroo_client.launch switcheroo_server.launch

gradle clean cleanEclipse downloadAssets genSources eclipse vscode
