docker build -t getmemory .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t getmemory`) DO (
SET IMAGE_ID=%%F
)

docker cp %IMAGE_ID%:/app/getMemory.exe .
docker cp %IMAGE_ID%:/app/getMemory-x64.exe .
docker cp %IMAGE_ID%:/app/getMemory-linux .
docker cp %IMAGE_ID%:/app/getMemory-x64-linux .
docker cp %IMAGE_ID%:/app/getMemory-osx .
docker cp %IMAGE_ID%:/app/getMemory-x64-osx .
