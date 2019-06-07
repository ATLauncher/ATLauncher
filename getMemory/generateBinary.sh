IMAGE_ID=`docker create -t getmemory .`

docker cp ${IMAGE_ID}:/app/getMemory.exe .
docker cp ${IMAGE_ID}:/app/getMemory-linux .
docker cp ${IMAGE_ID}:/app/getMemory-osx .
