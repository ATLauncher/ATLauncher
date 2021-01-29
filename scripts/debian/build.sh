docker build -t atlauncher/debian .
IMAGE_ID=`docker create -t atlauncher/debian .`

mkdir out

docker cp ${IMAGE_ID}:/work/atlauncher.deb ./out/atlauncher_1.0-1.deb
