cd ../../
docker run --rm -i -v $PWD:/work -w /work/scripts/installer amake/innosetup installer.iss
cd scripts/installer
