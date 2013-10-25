/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;


public class Server {

    private String name;
    private String baseURL;
    private boolean disabled;

    public Server(String name, String baseURL) {
        this.name = name;
        this.baseURL = baseURL;
        this.disabled = false;
    }

    public void disableServer() {
        this.disabled = true;
    }

    public void enableServer() {
        this.disabled = false;
    }

    public boolean isDisabled() {
        return this.disabled;
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

    public String toString() {
        if (this.disabled) {
            return "(X) " + this.name;
        } else {
            return this.name;
        }
    }
}
