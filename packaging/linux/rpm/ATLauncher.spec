Name:           atlauncher
Version:        1.0
Release:        1%{?dist}
Summary:        A launcher for Minecraft which integrates multiple different modpacks to allow you to download and install modpacks easily and quickly.

License:        GPLv3
URL:            https://atlauncher.com

BuildArch:      noarch

BuildRequires:  libappstream-glib
BuildRequires:  desktop-file-utils

Requires:       wget
Requires:       java >= 1:1.8.0
Provides:       atlauncher

%description
A launcher for Minecraft which integrates multiple different modpacks to allow you to download and install modpacks easily and quickly.

%prep


%build

%check
appstream-util validate-relax --nonet %{buildroot}%{_metainfodir}/atlauncher.metainfo.xml
desktop-file-validate %{buildroot}/%{_datadir}/applications/atlauncher.desktop

%install
mkdir -p %{buildroot}/usr/bin
install -m 0755 atlauncher %{buildroot}/usr/bin/atlauncher

mkdir -p %{buildroot}/usr/share/pixmaps
install -m 0644 atlauncher.png %{buildroot}/usr/share/pixmaps/atlauncher.png

mkdir -p %{buildroot}/usr/share/icons/hicolor/scalable/apps
install -m 0644 atlauncher.svg %{buildroot}/usr/share/icons/hicolor/scalable/apps/atlauncher.svg

mkdir -p %{buildroot}/%{_datadir}/applications
install -m 0644 atlauncher.desktop %{buildroot}/%{_datadir}/applications/atlauncher.desktop

mkdir -p %{buildroot}/%{_metainfodir}
install -m 0644 atlauncher.metainfo.xml %{buildroot}/%{_metainfodir}/atlauncher.metainfo.xml

%files
/usr/bin/atlauncher
/usr/share/pixmaps/atlauncher.png
/usr/share/icons/hicolor/scalable/apps/atlauncher.svg
%{_datadir}/applications/atlauncher.desktop
%{_metainfodir}/atlauncher.metainfo.xml


%changelog

* Mon Feb 01 2021 RyanTheAllmighty <ryan.dowling@atlauncher.com> - 1.0-1
- Initial build
