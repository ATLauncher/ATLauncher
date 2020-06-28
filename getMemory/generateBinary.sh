docker build -t getmemory .
IMAGE_ID=`docker create -t getmemory .`

docker cp ${IMAGE_ID}:/app/dist .
