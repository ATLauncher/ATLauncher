del dist/*
docker image rm atlauncher/getsysteminfo
docker build --rm -t atlauncher/getsysteminfo .
IMAGE_ID=`docker create -t atlauncher/getsysteminfo .`

docker cp ${IMAGE_ID}:/app/dist .
docker rm ${IMAGE_ID}
