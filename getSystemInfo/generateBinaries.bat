docker build -t atlauncher/getsysteminfo .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t atlauncher/getsysteminfo`) DO (
SET IMAGE_ID=%%F
)

docker cp %IMAGE_ID%:/app/dist .
