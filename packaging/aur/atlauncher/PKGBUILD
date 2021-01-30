# Maintainer: Alan Jenkins <alan.james.jenkins at gmail dot com>
# Maintainer: Ryan Dowling <ryan at ryandowling dot me>
# Contributor: Maximilian Berger <snowdragon92 at gmail dot com>
# Contributor: Cobalt Space <cobaltspace at protonmail dot com>

pkgname=atlauncher
_upstreamname=ATLauncher
pkgrel=1
pkgver=3.4.2.6
pkgdesc="A Launcher for Minecraft which integrates multiple different ModPacks to allow you to download and install
ModPacks easily and quickly."
arch=('any')
url="https://github.com/ATLauncher/ATLauncher"
license=('GPL3')
depends=('java-runtime=8' 'openal')
makedepends=('java-environment=8' 'gradle')
provides=('atlauncher')
conflicts=('atlauncher-bin')

source=("$_upstreamname-$pkgver.tar.gz::https://github.com/ATLauncher/ATLauncher/archive/v$pkgver.tar.gz"
        "atlauncher"
        "atlauncher.desktop"
        "atlauncher.png")

sha256sums=('6a5303a2f15f473409c7cfd46c30fb10791ba996a432fb7b66052ebe74a3a5a1'
            'a1184d3b8ed125b6a182871bb19851c0635806c29f3d392660ae716a61174a89'
            'bc8052811b1bd96c7b24963f11168ddba5e2769faa135a0e5680d6d1cc7b802a'
            'dd370888c78fdb652d656d97e4a7f7e8c90aa8d75d4f4d01d0bd32e95c327c47')

build() {
  cd "$_upstreamname-$pkgver"
  gradle build -x test
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
}
