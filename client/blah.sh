#!/bin/bash

./gradlew clean build
cp build/libs/client.jar ..
cd ..
java -jar client.jar&
cd client
java -jar build/libs/client.jar&
