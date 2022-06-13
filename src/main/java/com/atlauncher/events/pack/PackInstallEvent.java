package com.atlauncher.events.pack;

import com.atlauncher.data.Pack;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modrinth.ModrinthSearchHit;
import com.atlauncher.data.technic.TechnicModpackSlim;
import com.atlauncher.events.AnalyticsAction;
import com.atlauncher.events.AnalyticsCategory;

public final class PackInstallEvent extends PackEvent{
    public enum PackType implements AnalyticsCategory {
        ATLAUNCHER("ATLauncherPack"),
        TECHNIC("TechnicPack"),
        FTB("FTBPack"),
        MODRINTH("Modrinth");

        private final String value;

        PackType(final String value){
            this.value = value;
        }

        @Override
        public String getAnalyticsCategory(){
            return this.value;
        }
    }

    public enum InstallType implements AnalyticsAction {
        INSTALL("Install"),
        INSTALL_SERVER("ServerInstall");

        private final String value;

        InstallType(final String value){
            this.value = value;
        }

        @Override
        public String getAnalyticsValue() {
            return this.value;
        }
    }

    PackInstallEvent(final String name, final String version, final InstallType installType, final PackType packType){
        super(name, version, installType, packType);
    }

    public static PackInstallEvent newInstall(final String name, final PackType type){
        return new PackInstallEvent(name, "", InstallType.INSTALL, type);
    }

    public static PackInstallEvent newInstall(final Pack pack){
        return newInstall(pack.getName(), PackType.ATLAUNCHER);
    }

    public static PackInstallEvent newInstall(final ModpacksChPackManifest pack){
        return newInstall(pack.name, PackType.FTB);
    }

    public static PackInstallEvent newInstall(final ModrinthSearchHit pack){
        return newInstall(pack.title, PackType.MODRINTH);
    }

    public static PackInstallEvent newInstall(final TechnicModpackSlim pack){
        return newInstall(pack.name, PackType.TECHNIC);
    }

    public static PackInstallEvent newServerInstall(final String name, final String version, final PackType type){
        return new PackInstallEvent(name, version, InstallType.INSTALL_SERVER, type);
    }

    public static PackInstallEvent newServerInstall(final Pack pack){
        return newServerInstall(pack.name, "", PackType.ATLAUNCHER);
    }

    public static PackInstallEvent newServerInstall(final ModrinthSearchHit pack){
        return newServerInstall(pack.title, "", PackType.MODRINTH);
    }
}