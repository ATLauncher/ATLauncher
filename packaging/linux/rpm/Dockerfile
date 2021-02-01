FROM fedora:33
LABEL maintainer="ryan.dowling@atlauncher.com"

# install dependencies
RUN yum install -y rpmdevtools libappstream-glib desktop-file-utils \
    && yum clean all\
    && rm -rf /var/cache/yum

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
