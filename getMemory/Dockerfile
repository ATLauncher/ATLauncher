FROM golang:latest

RUN mkdir /app
ADD getMemory.go /app/
WORKDIR /app
RUN go get github.com/pbnjay/memory
RUN GOOS=windows GOARCH=386 go build -o getMemory.exe getMemory.go
RUN GOOS=linux GOARCH=386 go build -o getMemory-linux getMemory.go
RUN GOOS=darwin GOARCH=386 go build -o getMemory-osx getMemory.go
