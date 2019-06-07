# getMemory

This small go program simply prints out the detected system ram in the system

To build you will need to have go installed, and then run:

```sh
go get github.com/pbnjay/memory
GOOS=windows GOARCH=386 go build -o getMemory.exe getMemory.go
GOOS=linux GOARCH=386 go build -o getMemory-linux getMemory.go
GOOS=darwin GOARCH=386 go build -o getMemory-osx getMemory.go
```

Alternatively create the Dockerfile:

```sh
docker create -t getmemory .
```

That will spit out an image id. You can then run:

```sh
docker cp 2a5d878266d8d0789c0941707bc6a714e6b3986b67999059ba782dc18e7063de:/app/getMemory.exe .
docker cp 2a5d878266d8d0789c0941707bc6a714e6b3986b67999059ba782dc18e7063de:/app/getMemory-linux .
docker cp 2a5d878266d8d0789c0941707bc6a714e6b3986b67999059ba782dc18e7063de:/app/getMemory-osx .
```

Alternatively just run:

```sh
./generateBinary.sh
// or
./generateBinary.bat
```
