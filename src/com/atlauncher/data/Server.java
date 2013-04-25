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
    
    public Server(String name, String baseURL) {
        this.name = name;
        this.baseURL = baseURL;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getFileURL(String file) {
        return "http://" + this.baseURL + "/" + file;
    }
    
    public String toString() {
        return this.name;
    }
}
