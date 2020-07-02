FROM golang:1.14

RUN mkdir /app
ADD getMemory.go /app

WORKDIR /app

RUN apt-get update \
    && apt-get -y install gcc-arm-linux-gnueabi \
    && go get github.com/pbnjay/memory \
    #
    # 32 bit
    && GOOS=windows GOARCH=386 go build -o dist/getMemory.exe getMemory.go \
    && GOOS=linux GOARCH=386 go build -o dist/getMemory-linux getMemory.go \
    && GOOS=darwin GOARCH=386 go build -o dist/getMemory-osx getMemory.go \
    #
    # 64 bit
    && GOOS=windows GOARCH=amd64 go build -o dist/getMemory-x64.exe getMemory.go \
    && GOOS=linux GOARCH=amd64 go build -o dist/getMemory-x64-linux getMemory.go \
    && GOOS=darwin GOARCH=amd64 go build -o dist/getMemory-x64-osx getMemory.go \
    #
    # 32 bit arm
    && CC=arm-linux-gnueabi-gcc GOOS=windows GOARCH=arm GOARM=5 go build -o dist/getMemory-arm.exe getMemory.go \
    && CC=arm-linux-gnueabi-gcc GOOS=linux GOARCH=arm GOARM=5 go build -o dist/getMemory-arm-linux getMemory.go \
    && CC=arm-linux-gnueabi-gcc GOOS=darwin GOARCH=arm GOARM=5 go build -o dist/getMemory-arm-osx getMemory.go \
    #
    # 64 bit arm
    && CC=arm-linux-gnueabi-gcc GOOS=windows GOARCH=arm64 GOARM=5 go build -o dist/getMemory-arm64.exe getMemory.go \
    && CC=arm-linux-gnueabi-gcc GOOS=linux GOARCH=arm64 GOARM=5 go build -o dist/getMemory-arm64-linux getMemory.go \
    && CC=arm-linux-gnueabi-gcc GOOS=darwin GOARCH=arm64 GOARM=5 go build -o dist/getMemory-arm64-osx getMemory.go
