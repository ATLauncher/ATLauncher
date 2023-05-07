# Maintainer: Alan Jenkins <alan.james.jenkins at gmail dot com>
# Maintainer: Ryan Dowling <ryan at ryandowling dot me>
# Contributor: Maximilian Berger <snowdragon92 at gmail dot com>
# Contributor: Cobalt Space <cobaltspace at protonmail dot com>

pkgname=atlauncher
_upstreamname=ATLauncher
pkgrel=1
pkgver=3.4.28.1
pkgdesc="A launcher for Minecraft which integrates multiple different modpacks to allow you to download and install
modpacks easily and quickly."
arch=('any')
url="https://github.com/ATLauncher/ATLauncher"
license=('GPL3')
depends=('java-runtime>=17' 'openal')
makedepends=('java-environment>=17')
provides=('atlauncher')
conflicts=('atlauncher-bin')

source=("$_upstreamname-$pkgver.tar.gz::https://github.com/ATLauncher/ATLauncher/archive/v$pkgver.tar.gz"
        "atlauncher"
        "atlauncher.desktop"
        "atlauncher.png"
        "atlauncher.svg")

sha256sums=('1276d88c6228458a5c2b564b60794c3744bcc96b62f0ece0bcc34faca234c3e3'
            '5fd73a6159b9407a732f5956f58b3c9a890699fe14760c6e4f93f9876a32e635'
            '2e7eed9ae7174e0c94ff0a71d3735b73b6742f911033dccd6b52853d511d766c'
            'dd370888c78fdb652d656d97e4a7f7e8c90aa8d75d4f4d01d0bd32e95c327c47'
            '5e8aa9b202e69296b649d8d9bcf92083a05426e9480487aeea606c2490a2c5fa')

build() {
  cd "$_upstreamname-$pkgver"

  chmod 0755 ./gradlew
  ./gradlew build -x test
}

package() {
  cd "$srcdir"

  # create folder for the main jar executable
  mkdir -p "$pkgdir/usr/share/java/atlauncher/"
  chmod -R 755 "$pkgdir/usr/share/java/atlauncher/"

  # create folder for other files
  mkdir -p "$pkgdir/usr/share/atlauncher/Downloads"
  chmod 777 "$pkgdir/usr/share/atlauncher/Downloads"

  # install shell wrapper script
  install -D -m755 "$srcdir/atlauncher" "$pkgdir/usr/bin/atlauncher"

  # install jar
  install -D -m644 "$srcdir/$_upstreamname-$pkgver/dist/$_upstreamname-$pkgver.jar" "$pkgdir/usr/share/java/atlauncher/ATLauncher.jar"

  # install desktop launcher with icon
  install -D -m644 "$srcdir/atlauncher.desktop" "$pkgdir/usr/share/applications/atlauncher.desktop"
  install -D -m644 "$srcdir/atlauncher.png" "$pkgdir/usr/share/pixmaps/atlauncher.png"
  install -D -m644 "$srcdir/atlauncher.svg" "$pkgdir/usr/share/icons/hicolor/scalable/apps/atlauncher.svg"
}
