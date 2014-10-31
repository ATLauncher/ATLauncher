/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.data.mojang.auth;

public class ValidateRequest {
    private String accessToken;

    public ValidateRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}
