#!/bin/bash

# Advertising Company Management System - Run Script
# This script compiles and runs the JavaFX application

echo "========================================="
echo "Advertising Company Management System"
echo "========================================="
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null
then
    echo "ERROR: Maven is not installed!"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null
then
    echo "ERROR: Java is not installed!"
    echo "Please install Java JDK 17+ from: https://adoptium.net/"
    exit 1
fi

echo "Cleaning and compiling the project..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "Compilation successful! Starting application..."
    echo ""
    mvn javafx:run
else
    echo ""
    echo "ERROR: Compilation failed. Please check the error messages above."
    exit 1
fi
