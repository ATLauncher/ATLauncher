/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
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
