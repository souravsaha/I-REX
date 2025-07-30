#!/bin/bash

#if [ $# -eq 0 ] 
#then
#    echo "Usage: i-rex <path-to-index>";
#    exit 1;
#fi
#
irexJarPath=target/i-rex-1.1.jar
if test -f "$irexJarPath";
then
    mvn exec:java -Dexec.mainClass="irex.IRex" -Dexec.args="$1"
else
    echo "Not setup yet."
#    java -cp $HOME/bin/dist/i-rex.jar irex.IRex $1
fi

