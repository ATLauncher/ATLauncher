docker build -t getmemory .
IMAGE_ID=`docker create -t getmemory .`

del dist/*
del dist

docker cp ${IMAGE_ID}:/app/dist .
