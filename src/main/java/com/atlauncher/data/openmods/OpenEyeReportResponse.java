/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.openmods;

import com.atlauncher.data.Language;
import com.atlauncher.utils.Utils;

/**
 * The Class OpenEyeReportResponse contains information returned from OpenMods OpenEye system when a pending crash
 * report is reported through their API.
 */
public class OpenEyeReportResponse {
    /**
     * The type of this response. Generally is only ever 'known crash'.
     */
    private String type;

    /**
     * The url to the OpenEye website containing this crashes information.
     */
    private String url;

    /**
     * The note added to the crash, added by the mods developer, if any. Will return null if there has been no note
     * added.
     */
    private String note;

    /**
     * Gets the type of the crash that was reported.
     *
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the url to the OpenEye website with the details of this crash.
     *
     * @return the url to the OpenEye website
     */
    public String getURL() {
        return this.url;
    }

    /**
     * Gets the note associated with this reported crash, if any.
     *
     * @return the note or null if none
     */
    public String getNote() {
        return this.getNote();
    }

    /**
     * Checks if there is any note added for this response.
     *
     * @return true, if there is an attached note
     */
    public boolean hasNote() {
        return (this.note != null);
    }

    /**
     * Gets the display of the note for showing to the user.
     *
     * @return the string to add to the dialog box for the note
     */
    public String getNoteDisplay() {
        if (this.hasNote()) {
            return Language.INSTANCE.localize("instance.openeyehasnote") + "<br/><br/>" + Utils.splitMultilinedString
                    (this.getNote(), 100, "<br/>") + "<br/><br/>";
        } else {
            return Language.INSTANCE.localize("instance.openeyenonote") + "<br/><br/>";
        }
    }
}
