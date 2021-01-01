/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.data.openmods;

import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

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
            return GetText.tr("A note attached to the crash can be seen below:") + "<br/><br/>" + Utils.splitMultilinedString
                    (this.getNote(), 100, "<br/>") + "<br/><br/>";
        } else {
            return GetText.tr("There is no note attached to this crash.") + "<br/><br/>";
        }
    }
}
