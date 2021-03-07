FROM archlinux:latest

# makepkg cannot (and should not) be run as root:
RUN useradd -m notroot

# https://github.com/actions/virtual-environments/issues/2658
RUN patched_glibc=glibc-linux4-2.33-4-x86_64.pkg.tar.zst && \
    curl -LO "https://repo.archlinuxcn.org/x86_64/$patched_glibc" && \
    bsdtar -C / -xvf "$patched_glibc" && \
    echo "IgnorePkg   = glibc" >> /etc/pacman.conf

# Generally, refreshing without sync'ing is discouraged, but we've a clean
# environment here.
RUN pacman -Sy --noconfirm archlinux-keyring openssh base-devel git pacman-contrib namcap && \
    pacman -Syu --noconfirm

# Allow notroot to run stuff as root (to install dependencies):
RUN echo "notroot ALL=(ALL) NOPASSWD: ALL" > /etc/sudoers.d/notroot

# Continue execution (and CMD) as notroot:
USER notroot

# Auto-fetch GPG keys (for checking signatures):
RUN mkdir /home/notroot/.gnupg && \
    touch /home/notroot/.gnupg/gpg.conf && \
    echo "keyserver-options auto-key-retrieve" > /home/notroot/.gnupg/gpg.conf

# Install yay (for building AUR dependencies):
RUN git clone https://aur.archlinux.org/yay-bin.git /home/notroot/yay-bin && \
    cd /home/notroot/yay-bin && \
    makepkg --noconfirm --syncdeps --rmdeps --install --clean

COPY entrypoint.sh /entrypoint.sh

USER root
ENTRYPOINT ["/entrypoint.sh"]
