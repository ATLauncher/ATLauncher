FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t getmemory`) DO (
SET IMAGE_ID=%%F
)

docker cp %IMAGE_ID%:/app/getMemory.exe .
docker cp %IMAGE_ID%:/app/getMemory-linux .
docker cp %IMAGE_ID%:/app/getMemory-osx .
