/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.data;

public class Server {

    private String name;
    private String baseURL;
    private boolean userSelectable;
    private boolean disabled;
    private boolean isMaster;

    public Server(String name, String baseURL, boolean userSelectable, boolean isMaster) {
        this.name = name;
        this.baseURL = baseURL;
        this.userSelectable = userSelectable;
        this.disabled = false;
        this.isMaster = isMaster;
    }

    public boolean isMaster() {
        return this.isMaster;
    }

    public void disableServer() {
        this.disabled = true;
    }

    public void enableServer() {
        this.disabled = false;
    }

    public boolean isUserSelectable() {
        return this.userSelectable;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public String getHost() {
        return (this.baseURL.contains("/ATL") ? this.baseURL.replace("/ATL", "") : this.baseURL);
    }

    public String getName() {
        return this.name;
    }

    public String getFileURL(String file) {
        return "http://" + this.baseURL + "/" + file;
    }

    public String getTestURL() {
        return "http://" + this.baseURL + "/ping";
    }

    public void setUserSelectable(boolean selectable) {
        this.userSelectable = selectable;
    }

    public String toString() {
        if (this.disabled) {
            return "(X) " + this.name;
        } else {
            return this.name;
        }
    }
}
