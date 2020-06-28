FROM golang:1.14

RUN mkdir /app
ADD getMemory.go /app

WORKDIR /app

RUN go get github.com/pbnjay/memory \
    && GOOS=windows GOARCH=386 go build -o dist/getMemory.exe getMemory.go \
    && GOOS=linux GOARCH=386 go build -o dist/getMemory-linux getMemory.go \
    && GOOS=darwin GOARCH=386 go build -o dist/getMemory-osx getMemory.go \
    && GOOS=windows GOARCH=amd64 go build -o dist/getMemory-x64.exe getMemory.go \
    && GOOS=linux GOARCH=amd64 go build -o dist/getMemory-x64-linux getMemory.go \
    && GOOS=darwin GOARCH=amd64 go build -o dist/getMemory-x64-osx getMemory.go
