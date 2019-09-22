#!/bin/sh
docker run -it -v $(pwd):/app --workdir /app openjdk:11-jdk-slim /app/gradlew build
