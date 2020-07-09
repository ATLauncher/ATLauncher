FROM golang:1.14-alpine

RUN apk add upx && mkdir /app
ADD getSystemInfo.go /app
ADD go.mod /app
ADD go.sum /app

WORKDIR /app

RUN \
    #
    # Compile binaries
    GOOS=windows GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo.exe getSystemInfo.go \
    && GOOS=linux GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=386 go build -ldflags="-s -w" -o dist/getSystemInfo-osx getSystemInfo.go \
    && GOOS=windows GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64.exe getSystemInfo.go \
    && GOOS=linux GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=amd64 go build -ldflags="-s -w" -o dist/getSystemInfo-x64-osx getSystemInfo.go \
    #
    # Compress binaries with upx
    && upx -9 dist/*
