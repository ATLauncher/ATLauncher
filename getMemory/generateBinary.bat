docker build -t getmemory .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t getmemory`) DO (
SET IMAGE_ID=%%F
)

rm -rf dist/
docker cp %IMAGE_ID%:/app/dist .
