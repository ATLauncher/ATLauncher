cd ../../
docker run --rm -i -v $PWD:/work -w /work/packaging/windows-setup amake/innosetup installer.iss
cd packaging/windows-setup
