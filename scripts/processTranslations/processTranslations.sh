docker build -t atlauncher/processtranslations .
IMAGE_ID=`docker create -t atlauncher/processtranslations .`

docker cp ${IMAGE_ID}:/processTranslations/out ./out
