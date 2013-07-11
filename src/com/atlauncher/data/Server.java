/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
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
