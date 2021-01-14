docker build -t atlauncher/processtranslations .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t atlauncher/processtranslations`) DO (
SET IMAGE_ID=%%F
)

docker cp %IMAGE_ID%:/processTranslations/out ./out
