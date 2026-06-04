/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class OfflineAccountTest {
    @Test
    public void constructorSetsUsernameAndDerivesUuid() {
        OfflineAccount account = new OfflineAccount("Notch");

        assertEquals("Notch", account.username);
        assertEquals("Notch", account.minecraftUsername);
        // stored as 32 hex chars without dashes, matching MicrosoftAccount profile id format
        assertEquals(32, account.uuid.length());
        assertFalse(account.uuid.contains("-"));
    }

    @Test
    public void uuidUsesVanillaOfflineScheme() {
        UUID expected = UUID.nameUUIDFromBytes(
            "OfflinePlayer:Notch".getBytes(StandardCharsets.UTF_8));

        assertEquals(expected, OfflineAccount.offlineUUID("Notch"));
        // getRealUUID() (from AbstractAccount) must reconstruct the same UUID from the stored string
        assertEquals(expected, new OfflineAccount("Notch").getRealUUID());
    }

    @Test
    public void syntheticAuthValues() {
        OfflineAccount account = new OfflineAccount("Notch");

        assertEquals("0", account.getAccessToken());
        assertEquals("0", account.getSessionToken());
        assertEquals("legacy", account.getUserType());
        assertEquals("Notch", account.getCurrentUsername());
        assertNull(account.getSkinUrl());
    }
}
