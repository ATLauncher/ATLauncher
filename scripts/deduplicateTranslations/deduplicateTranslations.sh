cp ../../build/gettext/translations.pot in/translations.pot

docker build -t atlauncher/deduplicatetranslations .
IMAGE_ID=`docker create -t atlauncher/deduplicatetranslations .`

docker cp ${IMAGE_ID}:/deduplicateTranslations/out/translations.pot ./out/translations.pot
