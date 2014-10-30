/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.exceptions;

/**
 * InvalidMinecraftVersion is thrown when a given Minecraft version number isn't supported by the Launcher
 *
 * @author Ryan
 */
public class InvalidMinecraftVersion extends Exception {
    public InvalidMinecraftVersion(String message) {
        super(message);
    }
}
