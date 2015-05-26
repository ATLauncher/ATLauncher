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
package com.atlauncher.data.json;

import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.workers.InstanceInstaller;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Json
public enum ModType {
    JAR() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (installer.server && mod.getType(installer) == ModType.JAR) {
                FileUtils.unzip(mod.getFile(installer), installer.getTempJarDirectory());
                return;
            }

            FileUtils.copyFile(mod.getFile(installer), installer.jarmods);
            installer.addToJarOrder(mod.getFile());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.jarmods;
        }
    },
    DEPENDENCY() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.dependencies)) {
                FileUtils.createDirectory(installer.dependencies);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.dependencies);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    DEPANDENCY() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.dependencies)) {
                FileUtils.createDirectory(installer.dependencies);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.dependencies);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    FORGE() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (installer.server && mod.getType(installer) == ModType.FORGE) {
                FileUtils.copyFile(mod.getFile(installer), installer.root);
                return;
            }

            FileUtils.copyFile(mod.getFile(installer), installer.jarmods);
            installer.addToJarOrder(mod.getFile());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if (installer.server) {
                return installer.root;
            } else {
                return installer.jarmods;
            }
        }
    },
    MCPC() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (installer.server) {
                FileUtils.copyFile(mod.getFile(installer), installer.root);
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if (installer.server) {
                return installer.root;
            }

            return null;
        }
    },
    MODS() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            FileUtils.copyFile(mod.getFile(installer), installer.mods);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.mods;
        }
    },
    PLUGINS() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.plugins)) {
                FileUtils.createDirectory(installer.plugins);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.plugins);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.mods;
        }
    },
    IC2LIB() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.ic2)) {
                FileUtils.createDirectory(installer.ic2);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.ic2);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.ic2;
        }
    },
    DENLIB() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.denlib)) {
                FileUtils.createDirectory(installer.denlib);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.denlib);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.denlib;
        }
    },
    FLAN() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (Files.exists(installer.flans)) {
                FileUtils.createDirectory(installer.flans);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.flans);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.flans;
        }
    },
    COREMODS() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (installer.packVersion.getMinecraftVersion().usesCoreMods()) {
                if (!Files.exists(installer.coremods)) {
                    FileUtils.createDirectory(installer.coremods);
                }

                FileUtils.copyFile(mod.getFile(installer), installer.coremods);
            } else {
                FileUtils.copyFile(mod.getFile(installer), installer.mods);
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if (installer.packVersion.getMinecraftVersion().usesCoreMods()) {
                return installer.coremods;
            } else {
                return installer.mods;
            }
        }
    },
    EXTRACT() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            switch (mod.extractTo) {
                case coremods: {
                    if (installer.packVersion.getMinecraftVersion().usesCoreMods()) {
                        if (!Files.exists(installer.coremods)) {
                            FileUtils.createDirectory(installer.coremods);
                        }

                        FileUtils.unzip(mod.getFile(installer), installer.coremods);
                    } else {
                        FileUtils.unzip(mod.getFile(installer), installer.mods);
                    }
                    break;
                }
                case mods: {
                    FileUtils.unzip(mod.getFile(installer), installer.mods);
                    break;
                }
                case root: {
                    FileUtils.unzip(mod.getFile(installer), installer.root);
                    break;
                }
                default: {
                    LogManager.error("No known way to extract mod " + mod.name + " with type " + mod.extractTo);
                    break;
                }
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    DECOMP() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            Path tmpDecomp = FileSystem.TMP.resolve(mod.getSafeName());
            Path fileLoc = mod.getFile(installer);
            FileUtils.unzip(fileLoc, tmpDecomp);
            Path tmpDecompFile = tmpDecomp.resolve(mod.decompFile);
            if (Files.exists(tmpDecompFile)) {
                switch (mod.decompType) {
                    case coremods: {
                        if (Files.isRegularFile(tmpDecompFile)) {
                            if (installer.packVersion.getMinecraftVersion().usesCoreMods()) {
                                if (!Files.exists(installer.coremods)) {
                                    FileUtils.createDirectory(installer.coremods);
                                }

                                FileUtils.copyFile(tmpDecompFile, installer.coremods);
                            } else {
                                FileUtils.copyFile(tmpDecompFile, installer.mods);
                            }
                        } else {
                            if (installer.packVersion.getMinecraftVersion().usesCoreMods()) {
                                if (!Files.exists(installer.coremods)) {
                                    FileUtils.createDirectory(installer.coremods);
                                }

                                FileUtils.copyDirectory(tmpDecompFile, installer.coremods);
                            } else {
                                FileUtils.copyDirectory(tmpDecompFile, installer.mods);
                            }
                        }
                        break;
                    }
                    case jar: {
                        if (Files.isRegularFile(tmpDecompFile)) {
                            FileUtils.copyFile(tmpDecompFile, installer.jarmods);
                            installer.addToJarOrder(mod.decompFile);
                        } else {
                            Path newFile = installer.jarmods.resolve(mod.getSafeName() + ".zip");
                            FileUtils.zip(tmpDecompFile, newFile);
                            installer.addToJarOrder(mod.getSafeName() + ".zip");
                        }
                        break;
                    }
                    case mods: {
                        if (Files.isRegularFile(tmpDecompFile)) {
                            FileUtils.copyFile(tmpDecompFile, installer.mods);
                        } else {
                            FileUtils.copyDirectory(tmpDecompFile, installer.mods);
                        }
                        break;
                    }
                    case root: {
                        if (Files.isRegularFile(tmpDecompFile)) {
                            FileUtils.copyFile(tmpDecompFile, installer.root);
                        } else {
                            FileUtils.copyDirectory(tmpDecompFile, installer.root);
                        }
                        break;
                    }
                    default: {
                        LogManager.error("No known way to decomp mod " + mod.name + " with type " + mod.decompType);
                    }
                }
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    MILLENAIRE() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            Path fileLoc = mod.getFile(installer);
            Path tmpMillenaire = FileSystem.TMP.resolve(mod.getSafeName());
            FileUtils.unzip(fileLoc, tmpMillenaire);
            try (DirectoryStream<Path> stream1 = Files.newDirectoryStream(tmpMillenaire)) {
                for (Path p : stream1) {
                    try (DirectoryStream<Path> stream2 = Files.newDirectoryStream(p, this.dirFilter())) {
                        for (Path file : stream2) {
                            FileUtils.copyDirectory(file, installer.mods);
                        }
                    }
                }
            }
            FileUtils.delete(tmpMillenaire);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }

        private DirectoryStream.Filter<Path> dirFilter() {
            return new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path path) throws IOException {
                    return Files.isDirectory(path);
                }
            };
        }
    },
    TEXTUREPACK() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.texturepacks)) {
                FileUtils.createDirectory(installer.texturepacks);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.texturepacks);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.texturepacks;
        }
    },
    RESOURCEPACK() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.resourcepacks)) {
                FileUtils.createDirectory(installer.resourcepacks);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.resourcepacks);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.resourcepacks;
        }
    },
    TEXTUREPACKEXTRACT() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.texturepacks)) {
                FileUtils.createDirectory(installer.texturepacks);
            }

            FileUtils.unzip(mod.getFile(installer), installer.getTempTexturePacksDirectory());
            installer.setTexturePacksExtracted();
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    RESOURCEPACKEXTRACT() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.getTempResourcePacksDirectory())) {
                FileUtils.createDirectory(installer.resourcepacks);
            }

            FileUtils.unzip(mod.getFile(installer), installer.getTempResourcePacksDirectory());
            installer.setResourcePacksExtracted();
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    SHADERPACK() {
        @Override
        public void install(InstanceInstaller installer, Mod mod) throws Exception {
            if (!Files.exists(installer.shaderpacks)) {
                FileUtils.createDirectory(installer.shaderpacks);
            }

            FileUtils.copyFile(mod.getFile(installer), installer.shaderpacks);
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.shaderpacks;
        }
    };

    public abstract void install(InstanceInstaller installer, Mod mod) throws Exception;

    public abstract Path getInstallDirectory(InstanceInstaller installer, Mod mod);
}
