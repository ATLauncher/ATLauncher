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

import java.io.File;
import java.net.URISyntaxException;

public class Language {

    private String name;
    private String localizedName;
    private File file;

    public Language(String name, String localizedName) {
        this.name = name;
        this.localizedName = localizedName;
//        try {
//            this.file = new File(System.class.getResource(
//                    "/resources/languages/" + name + ".lang").toURI());
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getLocalizedName() {
        return this.localizedName;
    }
    
    public File getFile() {
        return file;
    }
    
    public String toString() {
        return this.localizedName;
    }
}
