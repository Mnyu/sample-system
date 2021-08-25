#!/bin/bash

sudo apt-get update

echo "-- Install JDK --"
sudo apt-get install -y openjdk-11-jdk

echo "-- Install Maven --"
sudo apt-get install -y maven

echo "-- Install NodeJS --"
curl -sL https://deb.nodesource.com/setup_12.x | sudo bash - && \
sudo apt-get install -y nodejs

echo "-- Done --"

