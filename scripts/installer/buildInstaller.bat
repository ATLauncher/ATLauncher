cd ../../
docker run --rm -i -v %cd%:/work -w /work/scripts/installer amake/innosetup installer.iss
cd scripts/installer
