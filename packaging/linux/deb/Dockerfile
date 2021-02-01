FROM debian:10
LABEL maintainer="ryan.dowling@atlauncher.com"

# add in all the files
ADD deb/control /work/atlauncher/DEBIAN/control
ADD deb/postrm /work/atlauncher/DEBIAN/postrm
ADD _common/atlauncher /work/atlauncher/usr/bin/atlauncher
ADD _common/atlauncher.desktop /work/atlauncher/usr/share/applications/atlauncher.desktop
ADD _common/atlauncher.metainfo.xml /work/atlauncher/usr/share/metainfo/atlauncher.metainfo.xml
ADD _common/atlauncher.png /work/atlauncher/usr/share/pixmaps/atlauncher.png
ADD _common/atlauncher.svg /work/atlauncher/usr/share/icons/hicolor/scalable/apps/atlauncher.svg

# chmod to what is needed
RUN chmod -R 0755 /work

# set the workdir
WORKDIR /work
