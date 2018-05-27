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
package com.atlauncher.data;

public class Server {

    private String name;
    private String baseURL;
    private boolean userSelectable;
    private boolean disabled;
    private boolean isMaster;
    private boolean isSecure;

    public Server(String name, String baseURL, boolean userSelectable, boolean isMaster, boolean isSecure) {
        this.name = name;
        this.baseURL = baseURL;
        this.userSelectable = userSelectable;
        this.disabled = false;
        this.isMaster = isMaster;
        this.isSecure = isSecure;
    }

    public boolean isMaster() {
        return this.isMaster;
    }

    public boolean isSecure() {
        return this.isSecure;
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
        return (this.baseURL.contains("/containers/atl") ? this.baseURL.replace("/containers/atl", "") : this.baseURL);
    }

    public String getName() {
        return this.name;
    }

    private String getProtocol() {
        return "http" + (this.isSecure ? "s" : "") + "://";
    }

    public String getFileURL(String file) {
        return this.getProtocol() + this.baseURL + "/" + file;
    }

    public String getTestURL() {
        return this.getProtocol() + this.baseURL + "/ping";
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
