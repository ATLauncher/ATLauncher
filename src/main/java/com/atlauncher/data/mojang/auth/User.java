/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.auth;

import com.atlauncher.data.mojang.Property;

import java.util.List;

public class User {

    private String id;
    private List<Property> properties;

    public String getId() {
        return this.id;
    }

    public List<Property> getProperties() {
        return this.properties;
    }

}
