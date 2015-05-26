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
    JAR(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception{
            if(installer.isServer() && mod.getType(installer) == ModType.JAR){
                FileUtils.unzip(mod.getFile(installer), installer.getTempJarDirectory());
                return;
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getJarModsDirectory());
            installer.addToJarOrder(mod.getFile());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getJarModsDirectory();
        }
    },
    DEPENDENCY(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getDependencyDirectory())){
                FileUtils.createDirectory(installer.getDependencyDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getDependencyDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    DEPANDENCY(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getDependencyDirectory())){
                FileUtils.createDirectory(installer.getDependencyDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getDependencyDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    FORGE(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception{
            if(installer.isServer() && mod.getType(installer) == ModType.FORGE){
                FileUtils.copyFile(mod.getFile(installer), installer.getRootDirectory());
                return;
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getJarModsDirectory());
            installer.addToJarOrder(mod.getFile());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if(installer.isServer()){
                return installer.getRootDirectory();
            } else{
                return installer.getJarModsDirectory();
            }
        }
    },
    MCPC(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception{
            if(installer.isServer()){
                FileUtils.copyFile(mod.getFile(installer), installer.getRootDirectory());
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if(installer.isServer()){
                return installer.getRootDirectory();
            }

            return null;
        }
    },
    MODS(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            FileUtils.copyFile(mod.getFile(installer), installer.getModsDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getModsDirectory();
        }
    },
    PLUGINS(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getPluginsDirectory())){
                FileUtils.createDirectory(installer.getPluginsDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getPluginsDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getModsDirectory();
        }
    },
    IC2LIB(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getIC2LibDirectory())){
                FileUtils.createDirectory(installer.getIC2LibDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getIC2LibDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getIC2LibDirectory();
        }
    },
    DENLIB(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getDenLibDirectory())){
                FileUtils.createDirectory(installer.getDenLibDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getDenLibDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getDenLibDirectory();
        }
    },
    FLAN(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(Files.exists(installer.getFlanDirectory())){
                FileUtils.createDirectory(installer.getFlanDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getFlanDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getFlanDirectory();
        }
    },
    COREMODS(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(installer.getVersion().getMinecraftVersion().usesCoreMods()){
                if(!Files.exists(installer.getCoreModsDirectory())){
                    FileUtils.createDirectory(installer.getCoreModsDirectory());
                }

                FileUtils.copyFile(mod.getFile(installer), installer.getCoreModsDirectory());
            } else{
                FileUtils.copyFile(mod.getFile(installer), installer.getModsDirectory());
            }
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            if(installer.getVersion().getMinecraftVersion().usesCoreMods()){
                return installer.getCoreModsDirectory();
            } else{
                return installer.getModsDirectory();
            }
        }
    },
    EXTRACT(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            switch(mod.extractTo){
                case coremods:{
                    if(installer.getVersion().getMinecraftVersion().usesCoreMods()){
                        if(!Files.exists(installer.getCoreModsDirectory())){
                            FileUtils.createDirectory(installer.getCoreModsDirectory());
                        }

                        FileUtils.unzip(mod.getFile(installer), installer.getCoreModsDirectory());
                    } else{
                        FileUtils.unzip(mod.getFile(installer), installer.getModsDirectory());
                    }
                    break;
                }
                case mods:{
                    FileUtils.unzip(mod.getFile(installer), installer.getModsDirectory());
                    break;
                }
                case root:{
                    FileUtils.unzip(mod.getFile(installer), installer.getRootDirectory());
                    break;
                }
                default:{
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
    DECOMP(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            Path tmpDecomp = FileSystem.TMP.resolve(mod.getSafeName());
            Path fileLoc = mod.getFile(installer);
            FileUtils.unzip(fileLoc, tmpDecomp);
            Path tmpDecompFile = tmpDecomp.resolve(mod.decompFile);
            if(Files.exists(tmpDecompFile)){
                switch(mod.decompType){
                    case coremods:{
                        if(Files.isRegularFile(tmpDecompFile)){
                            if(installer.getVersion().getMinecraftVersion().usesCoreMods()){
                                if(!Files.exists(installer.getCoreModsDirectory())){
                                    FileUtils.createDirectory(installer.getCoreModsDirectory());
                                }

                                FileUtils.copyFile(tmpDecompFile, installer.getCoreModsDirectory());
                            } else{
                                FileUtils.copyFile(tmpDecompFile, installer.getModsDirectory());
                            }
                        } else{
                            if(installer.getVersion().getMinecraftVersion().usesCoreMods()){
                                if(!Files.exists(installer.getCoreModsDirectory())){
                                    FileUtils.createDirectory(installer.getCoreModsDirectory());
                                }

                                FileUtils.copyDirectory(tmpDecompFile, installer.getCoreModsDirectory());
                            } else{
                                FileUtils.copyDirectory(tmpDecompFile, installer.getModsDirectory());
                            }
                        }
                        break;
                    }
                    case jar:{
                        if(Files.isRegularFile(tmpDecompFile)){
                            FileUtils.copyFile(tmpDecompFile, installer.getJarModsDirectory());
                            installer.addToJarOrder(mod.decompFile);
                        } else{
                            Path newFile = installer.getJarModsDirectory().resolve(mod.getSafeName() + ".zip");
                            FileUtils.zip(tmpDecompFile, newFile);
                            installer.addToJarOrder(mod.getSafeName() + ".zip");
                        }
                        break;
                    }
                    case mods:{
                        if(Files.isRegularFile(tmpDecompFile)){
                            FileUtils.copyFile(tmpDecompFile, installer.getModsDirectory());
                        } else{
                            FileUtils.copyDirectory(tmpDecompFile, installer.getModsDirectory());
                        }
                        break;
                    }
                    case root:{
                        if(Files.isRegularFile(tmpDecompFile)){
                            FileUtils.copyFile(tmpDecompFile, installer.getRootDirectory());
                        } else{
                            FileUtils.copyDirectory(tmpDecompFile, installer.getRootDirectory());
                        }
                        break;
                    }
                    default:{
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
    MILLENAIRE(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            Path fileLoc = mod.getFile(installer);
            Path tmpMillenaire = FileSystem.TMP.resolve(mod.getSafeName());
            FileUtils.unzip(fileLoc, tmpMillenaire);
            try(DirectoryStream<Path> stream1 = Files.newDirectoryStream(tmpMillenaire)){
                for(Path p : stream1){
                    try(DirectoryStream<Path> stream2 = Files.newDirectoryStream(p, this.dirFilter())){
                        for(Path file : stream2){
                            FileUtils.copyDirectory(file, installer.getModsDirectory());
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

        private DirectoryStream.Filter<Path> dirFilter(){
            return new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path path)
                throws IOException {
                    return Files.isDirectory(path);
                }
            };
        }
    },
    TEXTUREPACK(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception{
            if(!Files.exists(installer.getTexturePacksDirectory())){
                FileUtils.createDirectory(installer.getTexturePacksDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getTexturePacksDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getTexturePacksDirectory();
        }
    },
    RESOURCEPACK(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getResourcePacksDirectory())){
                FileUtils.createDirectory(installer.getResourcePacksDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getResourcePacksDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getResourcePacksDirectory();
        }
    },
    TEXTUREPACKEXTRACT(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getTexturePacksDirectory())){
                FileUtils.createDirectory(installer.getTexturePacksDirectory());
            }

            FileUtils.unzip(mod.getFile(installer), installer.getTempTexturePackDirectory());
            installer.setTexturePackExtracted();
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    RESOURCEPACKEXTRACT(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getTempResourcePackDirectory())){
                FileUtils.createDirectory(installer.getResourcePacksDirectory());
            }

            FileUtils.unzip(mod.getFile(installer), installer.getTempResourcePackDirectory());
            installer.setResourcePackExtracted();
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return null;
        }
    },
    SHADERPACK(){
        @Override
        public void install(InstanceInstaller installer, Mod mod)
        throws Exception {
            if(!Files.exists(installer.getShaderPacksDirectory())){
                FileUtils.createDirectory(installer.getShaderPacksDirectory());
            }

            FileUtils.copyFile(mod.getFile(installer), installer.getShaderPacksDirectory());
        }

        @Override
        public Path getInstallDirectory(InstanceInstaller installer, Mod mod) {
            return installer.getShaderPacksDirectory();
        }
    };

    public abstract void install(InstanceInstaller installer, Mod mod)
    throws Exception;

    public abstract Path getInstallDirectory(InstanceInstaller installer, Mod mod);
}
