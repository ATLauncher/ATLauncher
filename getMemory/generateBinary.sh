docker build -t getmemory .
IMAGE_ID=`docker create -t getmemory .`

docker cp ${IMAGE_ID}:/app/getMemory.exe .
docker cp ${IMAGE_ID}:/app/getMemory-x64.exe .
docker cp ${IMAGE_ID}:/app/getMemory-linux .
docker cp ${IMAGE_ID}:/app/getMemory-x64-linux .
docker cp ${IMAGE_ID}:/app/getMemory-osx .
docker cp ${IMAGE_ID}:/app/getMemory-x64-osx .
