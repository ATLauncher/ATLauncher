/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
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
