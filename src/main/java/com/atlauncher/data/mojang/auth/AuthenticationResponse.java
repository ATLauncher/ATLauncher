/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data.mojang.auth;

import com.atlauncher.App;
import com.atlauncher.data.mojang.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationResponse {

    private String accessToken;
    private String clientToken;
    private GameProfile selectedProfile;
    private GameProfile[] availableProfiles;
    private Map<String, Collection<String>> userProperties = new HashMap<String, Collection<String>>();
    private User user;

    private String uuid;

    private boolean isReal = true;

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
            this.availableProfiles = new GameProfile[]{gp};
            this.userProperties = new HashMap<String, Collection<String>>();
        }
        this.isReal = false;
    }

    public boolean isReal() {
        return this.isReal;
    }

    public String getUUID() {
        return this.uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public void setErrorMessage(String message) {
        this.error = message;
        this.errorMessage = message;
    }

    public boolean hasError() {
        if (this.errorMessage == null && this.selectedProfile == null && (this.user != null && this.user.getId() !=
                null)) {
            this.error = "There are no copies of Minecraft associated with this account!";
            this.errorMessage = "There are no copies of Minecraft associated with this account!";
        }
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
        this.userProperties = new HashMap<String, Collection<String>>();
        if ((this.user != null) && (this.user.getProperties() != null)) {
            for (Property property : this.user.getProperties()) {
                Collection<String> values = (Collection<String>) this.userProperties.get(property.getKey());

                if (values == null) {
                    values = new ArrayList<String>();
                    this.userProperties.put(property.getKey(), values);
                }

                values.add(property.getValue());
            }
        }
        return this.userProperties;
    }

    public void setNewAccessToken(String newAccessToken) {
        this.accessToken = newAccessToken;
    }

    public void setNewClientToken(String newClientToken) {
        this.clientToken = newClientToken;
    }

    public String getName(String username) {
        if (this.selectedProfile != null) {
            return this.selectedProfile.getName();
        }
        if (this.user != null && this.user.getId() != null) {
            return username;
        }
        throw new NullPointerException("Cannot Get Username For Account!");
    }

}
