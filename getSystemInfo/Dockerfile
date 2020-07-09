FROM golang:1.14-alpine

RUN apk add upx && mkdir /app
ADD getSystemInfo.go /app
ADD go.mod /app
ADD go.sum /app

WORKDIR /app

RUN \
    #
    # Compile 32 bit binaries
    GOOS=windows GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo.exe getSystemInfo.go \
    && GOOS=linux GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo-osx getSystemInfo.go \
    #
    # Compile 64 bit binaries
    && GOOS=windows GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64.exe getSystemInfo.go \
    && GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64-osx getSystemInfo.go \
    #
    # Compile arm binaries
    && GOOS=linux GOARCH=arm go build -ldflags="-s -w" -o dist/getSystemInfo-arm-linux getSystemInfo.go \
    #
    # Compile arm64 binaries
    && GOOS=linux GOARCH=arm64 go build -ldflags="-s -w" -o dist/getSystemInfo-arm64-linux getSystemInfo.go

RUN \
    #
    # Compress binaries with upx
    upx -9 dist/*
