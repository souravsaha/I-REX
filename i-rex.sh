#!/bin/bash

if [ $# -eq 0 ] 
then
    echo "Usage: i-rex <path-to-index>";
    exit 1;
fi

irexJarPath=target/i-rex-i-rex.jar
if test -f "$irexJarPath";
then
    java -cp target/i-rex-i-rex.jar irex.IRex $1
    mvn exec:java -Dexec.mainClass="com.lucene.lucene8.indexer.NewsDocIndexer" -Dexec.args="$prop_name"
else
    java -cp $HOME/bin/dist/i-rex.jar irex.IRex $1
fi

