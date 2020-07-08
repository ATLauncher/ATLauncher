FROM golang:1.14

RUN mkdir /app
ADD getSystemInfo.go /app

WORKDIR /app

RUN go get github.com/jaypipes/ghw \
    && GOOS=windows GOARCH=386 go build -o dist/getSystemInfo.exe getSystemInfo.go \
    && GOOS=linux GOARCH=386 go build -o dist/getSystemInfo-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=386 go build -o dist/getSystemInfo-osx getSystemInfo.go \
    && GOOS=windows GOARCH=amd64 go build -o dist/getSystemInfo-x64.exe getSystemInfo.go \
    && GOOS=linux GOARCH=amd64 go build -o dist/getSystemInfo-x64-linux getSystemInfo.go \
    && GOOS=darwin GOARCH=amd64 go build -o dist/getSystemInfo-x64-osx getSystemInfo.go
