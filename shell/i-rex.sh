#!/bin/bash

if [ $# -eq 0 ] 
then
    echo "Usage: " $0 " <path-to-index>";
    exit 1;
fi

java -cp dist/i-rex.jar irex.IRex $1
