docker build -t atlauncher/getsysteminfo .
IMAGE_ID=`docker create -t atlauncher/getsysteminfo .`

docker cp ${IMAGE_ID}:/app/dist .
