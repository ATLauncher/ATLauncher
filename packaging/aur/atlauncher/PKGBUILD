# Maintainer: Alan Jenkins <alan.james.jenkins at gmail dot com>
# Maintainer: Ryan Dowling <ryan at ryandowling dot me>
# Contributor: Maximilian Berger <snowdragon92 at gmail dot com>
# Contributor: Cobalt Space <cobaltspace at protonmail dot com>

pkgname=atlauncher
_upstreamname=ATLauncher
pkgrel=1
pkgver=3.4.40.2
pkgdesc="A launcher for Minecraft which integrates multiple different modpacks to allow you to download and install
modpacks easily and quickly."
arch=('any')
url="https://github.com/ATLauncher/ATLauncher"
license=('GPL3')
depends=('java-runtime>=17' 'openal')
makedepends=('jdk17-openjdk')
provides=('atlauncher')
conflicts=('atlauncher-bin')

source=("$_upstreamname-$pkgver.tar.gz::https://github.com/ATLauncher/ATLauncher/archive/v$pkgver.tar.gz"
    "atlauncher"
    "atlauncher.desktop"
    "atlauncher.png"
    "atlauncher.svg")

sha256sums=('d2c2f53ddb5c9b6bd7aa1525e2b84c40a1c7a25e85f431034ee6c0a4d177cc8f'
            '5fd73a6159b9407a732f5956f58b3c9a890699fe14760c6e4f93f9876a32e635'
            '0cc9c38febd814680fbd936e7a0f3ca28adbf54f7d210559d2d53fe70791033d'
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
