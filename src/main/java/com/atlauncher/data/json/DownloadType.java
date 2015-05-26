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

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Language;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public enum DownloadType {
    SERVER(){
        @Override
        public void download(InstanceInstaller installer, Path to, Mod mod) {
            try{
                Downloadable dl = mod.generateDownloadable(to, installer, true);
                if(dl.needToDownload()){
                    dl.download();
                }
            } catch(Exception e){
                LogManager.logStackTrace(e);
            }
        }
    },
    BROWSER(){
        @Override
        public void download(InstanceInstaller installer, Path to, Mod mod)
        throws Exception{
            Path dlFile = (mod.server ? FileSystem.DOWNLOADS : FileSystem.USER_DOWNLOADS).resolve((mod.server ? mod.serverFile : mod.getFile()));
            if(Files.exists(dlFile)){
                FileUtils.moveFile(dlFile, to, true);
            }

            if(mod.fileCheck.equalsIgnoreCase("before") && mod.filePattern){
                List<String> files = FileUtils.listFiles(FileSystem.getDownloads(), this.getFilter(mod));
                if(files.size() == 1){
                    to = FileSystem.getDownloads().resolve(files.get(0));
                } else if(files.size() > 1){
                    for(int i = 0; i < files.size(); i++){
                        if(mod.filePreference.equalsIgnoreCase("first") && i == 0){
                            to = FileSystem.getDownloads().resolve(files.get(i));
                            break;
                        }

                        if(mod.filePreference.equalsIgnoreCase("last") && (i + 1) == files.size()){
                            to = FileSystem.getDownloads().resolve(files.get(i));
                            break;
                        }
                    }
                }
            }

            while(!Files.exists(to)){
                int ret = 1;
                do{
                    if(ret == 1){
                        Utils.openBrowser(mod.getUrl());
                    }
                    String[] options = new String[]{
                        Language.INSTANCE.localize("common.openfolder"),
                        Language.INSTANCE.localize("instance.ivedownloaded")
                    };
                    ret = JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            HTMLUtils.centerParagraph(
                                Language.INSTANCE.localizeWithReplace(
                                    "instance.browseropened", (mod.serverFile == null ?
                                    (mod.filePattern ? mod.name : mod.getFile()) :
                                    (mod.filePattern ? mod.name : mod.serverFile))
                                ) + "<br/><br/>" +
                                Language.INSTANCE.localize("instance.pleasesave") + "<br/><br/>" +
                                (App.settings.isUsingMacApp() ? FileSystem.USER_DOWNLOADS : (mod.filePattern ?
                                FileSystem.DOWNLOADS : FileSystem.DOWNLOADS + " or<br/>" + FileSystem.USER_DOWNLOADS))
                            ),
                            Language.INSTANCE.localize("common.downloading") + " " + (mod.serverFile == null ? (mod.filePattern ? mod.name : mod.getFile()) : (mod.filePattern ? mod.name : mod.serverFile)),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]
                    );

                    if(ret == JOptionPane.CLOSED_OPTION){
                        installer.cancel(true);
                        return;
                    } else if(ret == 0){
                        Utils.openExplorer(FileSystem.USER_DOWNLOADS);
                    }
                } while(ret != 1);

                if(mod.filePattern){
                    List<String> files = FileUtils.listFiles(FileSystem.getDownloads(), this.getFilter(mod));
                    if(files.size() == 1){
                        to = FileSystem.getDownloads().resolve(files.get(0));
                    } else if(files.size() > 1){
                        for(int i = 0; i < files.size(); i++){
                            if(mod.filePreference.equalsIgnoreCase("first") && i == 0){
                                to = FileSystem.getDownloads().resolve(files.get(i));
                                break;
                            }

                            if(mod.filePreference.equalsIgnoreCase("last") && (i + 1) == files.size()){
                                to = FileSystem.getDownloads().resolve(files.get(i));
                                break;
                            }
                        }
                    }
                } else{
                    if(!Files.exists(to)){
                        if(Files.exists(dlFile)){
                            FileUtils.moveFile(dlFile, to, true);
                        }

                        Path zip = FileSystem.DOWNLOADS.resolve((mod.server ? mod.serverFile : mod.getFile()) + ".zip");
                        if(Files.exists(zip)){
                            FileUtils.moveFile(zip, to, true);
                        } else{
                            zip = FileSystem.USER_DOWNLOADS.resolve((mod.server ? mod.serverFile : mod.getFile()) + ".zip");
                            if(Files.exists(zip)){
                                FileUtils.moveFile(zip, to, true);
                            }
                        }
                    }
                }
            }
        }

        private DirectoryStream.Filter<Path> getFilter(final Mod mod){
            return new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path path)
                throws IOException {
                    return path.getFileName().toString().matches(mod.name);
                }
            };
        }
    },
    DIRECT(){
        @Override
        public void download(InstanceInstaller installer, Path to, Mod mod) {
            try{
                Downloadable dl = mod.generateDownloadable(to, installer, false);
                if(dl.needToDownload()){
                    dl.download();
                }
            } catch(Exception e){
                LogManager.logStackTrace(e);
            }
        }
    };

    public abstract void download(InstanceInstaller installer, Path to, Mod mod)
    throws Exception;
}
