/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.api;

public class ProfileResponse {
    private String name;
    private String id;
    private boolean legacy;
    private boolean demo;

    public String getId() {
        return this.id;
    }
}
