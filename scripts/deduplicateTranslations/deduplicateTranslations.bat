copy ..\..\build\gettext\translations.pot in\translations.pot

docker build -t atlauncher/deduplicatetranslations .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t atlauncher/deduplicatetranslations`) DO (
SET IMAGE_ID=%%F
)

docker cp %IMAGE_ID%:/deduplicateTranslations/out/translations.pot ./out/translations.pot
