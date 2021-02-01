# Deb packaging
docker build -t atlauncher/atlauncher-packaging-linux-deb -f deb/Dockerfile .
docker run --rm -i -v $PWD/out:/work/out -w /work/out atlauncher/atlauncher-packaging-linux-deb dpkg-deb --build ../atlauncher atlauncher-1.0-1.deb

# RPM packaging
docker build -t atlauncher/atlauncher-packaging-linux-rpm -f rpm/Dockerfile .
docker run --rm -i -v $PWD/out:/root/rpmbuild/RPMS/noarch -w /work atlauncher/atlauncher-packaging-linux-rpm rpmbuild -bb --build-in-place --target noarch atlauncher.spec
