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
