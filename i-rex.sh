#!/bin/bash

if [ $# -eq 0 ] 
then
    echo "Usage: i-rex <path-to-index>";
    exit 1;
fi

irexJarPath=dist/i-rex.jar
if test -f "$irexJarPath";
then
    java -cp dist/i-rex.jar irex.IRex $1
else
    java -cp $HOME/bin/dist/i-rex.jar irex.IRex $1
fi

