FROM debian

ADD atlauncher /work/atlauncher

RUN chmod -R 0755 /work

WORKDIR /work

RUN dpkg-deb --build atlauncher
