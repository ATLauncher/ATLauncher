FROM fedora:37
LABEL maintainer="ryan.dowling@atlauncher.com"

# install dependencies
RUN dnf install -y rpmdevtools libappstream-glib desktop-file-utils \
    && dnf clean all\
    && rm -rf /var/cache/dnf

# add in all the files
ADD rpm/atlauncher.spec /work/atlauncher.spec
ADD _common/atlauncher /work/atlauncher
ADD _common/atlauncher.desktop /work/atlauncher.desktop
ADD _common/atlauncher.metainfo.xml /work/atlauncher.metainfo.xml
ADD _common/atlauncher.png /work/atlauncher.png
ADD _common/atlauncher.svg /work/atlauncher.svg

# chmod to what is needed
RUN chmod -R 0755 /work

# set the workdir
WORKDIR /work
