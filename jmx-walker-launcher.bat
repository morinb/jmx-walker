@echo off
rem JMX Walker launcher


git clone http://atpds4.fr.net.intra/bmo/jmx-walker.git jmx-walker
cd jmx-walker
sbt/bin/sbt.bat -mem 128 run