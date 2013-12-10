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
package com.atlauncher.data.mojang.auth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.data.mojang.Property;

public class AuthenticationResponse {

    private String accessToken;
    private String clientToken;
    private GameProfile selectedProfile;
    private GameProfile[] availableProfiles;
    private Map<String, Collection<String>> userProperties = new HashMap();
    private User user;

    private String error;
    private String errorMessage;
    private String cause;

    public AuthenticationResponse(String session, boolean error) {
        if (error) {
            this.error = session;
            this.errorMessage = session;
        } else {
            String[] parts = session.split(":");
            this.accessToken = parts[1];
            this.clientToken = parts[1];
            GameProfile gp = new GameProfile("0", App.settings.getAccount().getMinecraftUsername());
            this.selectedProfile = gp;
            this.availableProfiles = new GameProfile[] { gp };
            this.userProperties = new HashMap();
        }
    }

    public boolean hasError() {
        return this.errorMessage != null;
    }

    public String getError() {
        return this.error;
    }

    public String getErrorMessage() {
        if (this.errorMessage == null) {
            return null;
        }
        return this.errorMessage.replace("Invalid credentials. ", "");
    }

    public String getCause() {
        return this.cause;
    }

    public String getSession() {
        return "token:" + this.accessToken + ":" + this.selectedProfile.getId();
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getClientToken() {
        return this.clientToken;
    }

    public GameProfile[] getAvailableProfiles() {
        return this.availableProfiles;
    }

    public GameProfile getSelectedProfile() {
        return this.selectedProfile;
    }

    public User getUser() {
        return this.user;
    }

    public Map<String, Collection<String>> getProperties() {
        this.userProperties = new HashMap();
        if ((this.user != null) && (this.user.getProperties() != null)) {
            for (Property property : this.user.getProperties()) {
                Collection values = (Collection) this.userProperties.get(property.getKey());

                if (values == null) {
                    values = new ArrayList();
                    this.userProperties.put(property.getKey(), values);
                }

                values.add(property.getValue());
            }
        }
        return this.userProperties;
    }

}
