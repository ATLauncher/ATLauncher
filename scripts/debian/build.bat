docker build -t atlauncher/debian .
FOR /F "tokens=* USEBACKQ" %%F IN (`docker create -t atlauncher/debian`) DO (
SET IMAGE_ID=%%F
)

mkdir out

docker cp %IMAGE_ID%:/work/atlauncher.deb ./out/atlauncher_1.0-1.deb
