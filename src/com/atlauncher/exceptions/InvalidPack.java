/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.exceptions;


/**
 * InvalidPack is thrown when searching for a Pack by ID and that ID isn't found
 * 
 * @author Ryan
 */
public class InvalidPack extends Exception {

    public InvalidPack(String message) {
        super(message);
    }

}
