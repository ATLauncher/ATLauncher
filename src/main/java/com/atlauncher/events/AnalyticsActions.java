/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.events;

public enum AnalyticsActions implements AnalyticsAction{
    ADD_FABRIC_API("AddFabricApi"),
    VIEW_MODS("ViewMods"),
    CHANGE_THEME("ChangeTheme"),
    UPDATE_DATA("UpdateData"),
    UPDATE_SKIN("UpdateSkin"),
    DELETE("Delete"),
    UPDATE_USERNAME("UpdateUsername"),
    REFRESH_ACCESS_TOKEN("RefreshAccessToken"),
    EDIT("Edit"),
    SWITCH("Switch"),
    COPY_LOG("CopyLog"),
    INSTALL("Install"),
    UPLOAD_LOG("UploadLog"),
    KILL_MINECRAFT("KillMinecraft"),
    ADD_MODS("AddMods"),
    EDIT_MODS("EditMods"),
    SETTINGS("Settings"),
    EXPORT("Export"),
    ADD("Add"),
    ADD_FROM_ZIP("AddFromZip"),
    ADD_FROM_URL("AddFromUrl"),
    SEARCH("Search"),
    NEXT_PAGE("Next"),
    PREVIOUS_PAGE("Previous"),
    ADD_FILE("AddFile"),
    PLAY("Play"),
    PLAY_OFFLINE("PlayOffline"),
    REINSTALL("Reinstall"),
    RENAME("Rename"),
    CLONE("Clone"),
    CHANGE_IMAGE("ChangeImage"),
    CHANGE_LOADER_VERSION("ChangeLoaderVersion"),
    ADD_LOADER("AddLoader"),
    REMOVE_LOADER("RemoveLoader"),
    PACK_BACKUP("Backup"),
    ADD_QUILT_STANDARD_LIBRARIES("AddQuiltStandardLibraries"),
    UPDATE("Update"),
    UPDATE_FROM_PLAY("UpdateFromPlay"),
    SETUP_DIALOG_COMPLETE("SetupDialogComplete"),
    RUN("Run"),
    MAKE_SHARE_CODE("MakeShareCode");

    private final String value;

    AnalyticsActions(final String value){
        this.value = value;
    }

    @Override
    public String getAnalyticsValue(){
        return this.value;
    }
}
