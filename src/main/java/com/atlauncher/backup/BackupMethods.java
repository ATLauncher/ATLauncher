/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.backup;

import com.atlauncher.utils.FileUtils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.common.collect.ImmutableList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class BackupMethods {
    private static final List<BackupMethod> methods = ImmutableList.<BackupMethod>builder().add(new ReisBackupMethod
            ()).add(new ZansBackupMethod()).add(new NEIBackupMethod()).add(new OptionsBackupMethod()).add(new
            ServerBackupMethod()).add(new PortalGunSoundsBackupMethod()).build();

    public static void backup(InstanceInstaller installer) {
        for (BackupMethod method : methods) {
            method.backup(installer);
        }
    }

    public static void restore(InstanceInstaller installer) {
        for (BackupMethod method : methods) {
            method.restore(installer);
        }
    }

    private static final class PortalGunSoundsBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path portalGunSounds = installer.mods.resolve("PortalGunSounds.pak");
            if (Files.exists(portalGunSounds) && Files.isRegularFile(portalGunSounds)) {
                FileUtils.copyFile(portalGunSounds, installer.tmpDir);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path portalGunSounds = installer.tmpDir.resolve("PortalGunSounds.pak");
            if (Files.exists(portalGunSounds) && Files.isRegularFile(portalGunSounds)) {
                FileUtils.copyFile(portalGunSounds, installer.mods);
            }
        }
    }

    private static final class ServerBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path servers = installer.root.resolve("servers.dat");
            if (Files.exists(servers) && Files.isRegularFile(servers)) {
                FileUtils.copyFile(servers, installer.tmpDir);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path servers = installer.tmpDir.resolve("servers.dat");
            if (Files.exists(servers) && Files.isRegularFile(servers)) {
                FileUtils.copyFile(servers, installer.root);
            }
        }
    }

    private static final class OptionsBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path options = installer.root.resolve("options.txt");
            if (Files.exists(options) && Files.isRegularFile(options)) {
                FileUtils.copyFile(options, installer.tmpDir);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path options = installer.tmpDir.resolve("options.txt");
            if (Files.exists(options) && Files.isRegularFile(options)) {
                FileUtils.copyFile(options, installer.root);
            }
        }
    }

    private static final class ZansBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path zans = installer.mods.resolve("VoxelMods");
            if (Files.exists(zans) && Files.isDirectory(zans)) {
                FileUtils.copyDirectory(zans, installer.tmpDir, true);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path zans = installer.tmpDir.resolve("VoxelMods");
            if (Files.exists(zans) && Files.isDirectory(zans)) {
                FileUtils.copyDirectory(zans, installer.mods, true);
            }
        }
    }

    private static final class ReisBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path reis = installer.mods.resolve("rei_minimap");
            if (Files.exists(reis) && Files.isDirectory(reis)) {
                FileUtils.copyDirectory(reis, installer.tmpDir, true);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path reis = installer.tmpDir.resolve("rei_minimap");
            if (Files.exists(reis) && Files.isDirectory(reis)) {
                FileUtils.copyDirectory(reis, installer.mods, true);
            }
        }
    }

    private static final class NEIBackupMethod implements BackupMethod {
        @Override
        public void backup(InstanceInstaller installer) {
            Path cfg = installer.configs.resolve("NEI.cfg");
            if (Files.exists(cfg) && Files.isRegularFile(cfg)) {
                FileUtils.copyFile(cfg, installer.tmpDir);
            }
        }

        @Override
        public void restore(InstanceInstaller installer) {
            Path cfg = installer.tmpDir.resolve("NEI.cfg");
            if (Files.exists(cfg) && Files.isRegularFile(cfg)) {
                FileUtils.copyFile(cfg, installer.configs);
            }
        }
    }
}