# Maintainer: Alan Jenkins <alan.james.jenkins at gmail dot com>
# Maintainer: Ryan Dowling <ryan at ryandowling dot me>
# Contributor: Maximilian Berger <snowdragon92 at gmail dot com>

pkgname=atlauncher-bin
_upstreamname=ATLauncher
pkgrel=1
pkgver=3.4.28.1
pkgdesc="A launcher for Minecraft which integrates multiple different modpacks to allow you to download and install
modpacks easily and quickly."
arch=('any')
url="https://atlauncher.com/"
license=('GPL3')
depends=('java-runtime>=17' 'openal')
makedepends=('unzip')
provides=('atlauncher')
conflicts=('atlauncher')

source=("atlauncher-${pkgver}-${pkgrel}.jar::https://github.com/ATLauncher/ATLauncher/releases/download/v$pkgver/$_upstreamname-$pkgver.jar"
        "atlauncher"
        "atlauncher.desktop"
        "atlauncher.png"
        "atlauncher.svg")
noextract=("atlauncher-${pkgver}-${pkgrel}.jar")

sha256sums=('208c0331acf150c43b9c6424ff85446627200a6091e2847c518b4edfaa4212ae'
            '9cfb56200cc85f78df173f6775ae045b43c467b4e6cce1dcc0c50d42a90b42d5'
            '2e7eed9ae7174e0c94ff0a71d3735b73b6742f911033dccd6b52853d511d766c'
            'dd370888c78fdb652d656d97e4a7f7e8c90aa8d75d4f4d01d0bd32e95c327c47'
            '5e8aa9b202e69296b649d8d9bcf92083a05426e9480487aeea606c2490a2c5fa')

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
  install -D -m644 "$srcdir/atlauncher-${pkgver}-${pkgrel}.jar" "$pkgdir/usr/share/java/atlauncher/ATLauncher.jar"

  # install desktop launcher with icon
  install -D -m644 "$srcdir/atlauncher.desktop" "$pkgdir/usr/share/applications/atlauncher.desktop"
  install -D -m644 "$srcdir/atlauncher.png" "$pkgdir/usr/share/pixmaps/atlauncher.png"
  install -D -m644 "$srcdir/atlauncher.svg" "$pkgdir/usr/share/icons/hicolor/scalable/apps/atlauncher.svg"
}
