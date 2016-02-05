#!/usr/bin/env bash

if [ ! -d jmx-walker ]; then
    echo "Cloning jmx-walker"
    git clone http://atpds4.fr.net.intra/bmo/jmx-walker.git jmx-walker
fi

cd jmx-walker
echo "Getting last jmx-walker version"
git pull origin master
./sbt/bin/sbt -mem 128 run