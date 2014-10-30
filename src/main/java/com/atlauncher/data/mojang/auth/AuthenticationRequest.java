/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data.mojang.auth;

public class AuthenticationRequest {

    private Agent agent;
    private String username;
    private String password;
    private String clientToken;
    private boolean requestUser = true;

    public AuthenticationRequest(String username, String password, String clientToken) {
        this.agent = new Agent("Minecraft", 10);
        this.username = username;
        this.password = password;
        this.clientToken = clientToken;
    }

    public Agent getAgent() {
        return agent;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientToken() {
        return clientToken;
    }

    public boolean isRequestUser() {
        return requestUser;
    }

}
