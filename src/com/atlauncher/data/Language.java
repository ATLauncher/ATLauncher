/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.atlauncher.App;

public class Language {

    private String name;
    private String localizedName;
    private File file;
    private Properties properties;
    private Properties english;

    public Language(String name, String localizedName) {
        this.name = name;
        this.localizedName = localizedName;
        this.file = new File(App.settings.getLanguagesDir(), name.toLowerCase() + ".lang");
        properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        loadEnglishBackup();
    }
    
    private void loadEnglishBackup() {
        english = new Properties();
        try {
            english.load(new FileInputStream(new File(App.settings.getLanguagesDir(), "english.lang")));
        } catch (FileNotFoundException e) {
            App.settings.getConsole().logStackTrace(e);
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
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

    public String getString(String property) {
        if (properties.containsKey(property)) {
            return properties.getProperty(property);
        }else{
            return english.getProperty(property, "Unknown Property: " + property);
        }
    }

    public String toString() {
        return this.localizedName;
    }
}
