cd ../../
docker run --rm -i -v $PWD:/work -w /work/scripts/windows-setup amake/innosetup installer.iss
cd scripts/windows-setup
