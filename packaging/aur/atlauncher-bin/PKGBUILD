# Maintainer: Alan Jenkins <alan.james.jenkins at gmail dot com>
# Maintainer: Ryan Dowling <ryan at ryandowling dot me>
# Contributor: Maximilian Berger <snowdragon92 at gmail dot com>

pkgname=atlauncher-bin
_upstreamname=ATLauncher
pkgrel=1
pkgver=3.4.40.2
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

sha256sums=('b44ff8c3da35ff036b2163f012188ee5bcaf5976354d8d19b7619f9348d98ae0'
            '9cfb56200cc85f78df173f6775ae045b43c467b4e6cce1dcc0c50d42a90b42d5'
            '0cc9c38febd814680fbd936e7a0f3ca28adbf54f7d210559d2d53fe70791033d'
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
