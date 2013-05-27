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
package com.atlauncher.exceptions;

import com.atlauncher.gui.LauncherFrame;

/**
 * InvalidPack is thrown when searching for a Pack by ID and that ID isn't found
 * 
 * @author Ryan
 */
public class InvalidPack extends Exception {

    public InvalidPack(String message) {
        super(message);
        LauncherFrame.console.log("InvalidPack Exception Thrown: " + message);
    }

}
