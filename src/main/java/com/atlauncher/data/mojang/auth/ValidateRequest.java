/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data.mojang.auth;

public class ValidateRequest {
    private String accessToken;

    public ValidateRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}
