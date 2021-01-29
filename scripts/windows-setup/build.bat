cd ../../
docker run --rm -i -v %cd%:/work -w /work/scripts/windows-setup amake/innosetup installer.iss
cd scripts/windows-setup
