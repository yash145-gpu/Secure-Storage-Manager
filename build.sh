#!/bin/bash

#Compiles all source files and creates JAR , execute the JAR by <run.sh>

out="bin"
JAR_FILE="Secure-Storage.jar"
SQLITE_JAR="lib/sqlite-jdbc-3.42.0.0.jar"
SRC_DIR="src/main/java/"
RES_DIR="src/main/res/"
mkdir -p "$out"
cp -r "$RES_DIR"/* "$out"
javac -cp "$SQLITE_JAR" -d "$out" $(find "$SRC_DIR" -name "*.java")

if [ $? -eq 0 ]; then
    echo "Compilation successful." 
    jar cfm "$JAR_FILE" MANIFEST.MF -C "$out" . 
    echo "JAR file created: $JAR_FILE"
    
else
    echo "Error,Compilation failed."
fi

