package com.atlauncher.data;

import com.atlauncher.gui.components.CollapsiblePanel;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Extend this class if you wish to add a backup/sync method. You will need to track
 * any backups you make yourself.
 *
 * @author Kihira
 */
public abstract class SyncAbstract{
    public static final HashMap<String, SyncAbstract> syncList = new HashMap<String, SyncAbstract>();
    private final String syncName;

    public SyncAbstract(String name){
        if(syncList.containsKey(name)){
            throw new IllegalArgumentException("A sync handler with the name " + name + " has already been created!");
        } else{
            syncList.put(name, this);
            syncName = name;
        }
    }

    public String getName(){
        return syncName;
    }

    /**
     * This is called when a world needs to be backed up.
     *
     * @param backupName The name of the backup
     * @param worldData  The folder of the world
     * @param instance   The instance for the world
     */
    public abstract void backupWorld(String backupName, File worldData, Instance instance);

    /**
     * This should return a list of names of the backups that the current application has backed up
     *
     * @param instance The instance
     * @return A list of world names
     */
    public abstract List<String> getBackupsForInstance(Instance instance);

    /**
     * This is called when the user wants to restore a backup. It should restore the backup to the correct save location
     * This method is called from a new thread
     *
     * @param backupName The name of the backup to restore
     * @param instance   The instance that the save should be restored to
     */
    public abstract void restoreBackup(String backupName, Instance instance);

    public abstract void deleteBackup(String backupName, Instance instance);

    /**
     * If the sync addon has settings then you should return an instance of {@link com.atlauncher.gui.components.CollapsiblePanel CollapsiblePanel}.
     * It is recommended you extend that class. Look at {@link com.atlauncher.data.DropboxSync DropboxSync} for example.
     *
     * @return The settings panel
     */
    public abstract CollapsiblePanel getSettingsPanel();
}
