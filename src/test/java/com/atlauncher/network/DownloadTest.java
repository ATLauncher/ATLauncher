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
package com.atlauncher.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.modrinth.ModrinthDownloadMetadata;

import okhttp3.Headers;

public class DownloadTest {
    @Test
    public void curseForgeFileDownloadsIncludeApiKeyHeader() {
        Headers headers = Download.buildHeadersForUrl(
            "https://edge.forgecdn.net/files/1234/56/ExampleMod-2.0-1.20.1.jar", Collections.emptyMap());

        assertEquals(Constants.CURSEFORGE_CORE_API_KEY, headers.get(Constants.CURSEFORGE_API_KEY_HEADER));
    }

    @Test
    public void nonCurseForgeFileDownloadsDoNotIncludeApiKeyHeader() {
        Headers headers = Download.buildHeadersForUrl(
            "https://example.com/files/1234/56/ExampleMod-2.0-1.20.1.jar", Collections.emptyMap());

        assertNull(headers.get(Constants.CURSEFORGE_API_KEY_HEADER));
    }

    @Test
    public void explicitCurseForgeApiKeyHeaderIsPreserved() {
        Map<String, String> existingHeaders = new HashMap<>();
        existingHeaders.put("X-Api-Key", "custom-key");

        Headers headers = Download.buildHeadersForUrl(
            "https://edge.forgecdn.net/files/1234/56/ExampleMod-2.0-1.20.1.jar", existingHeaders);

        assertEquals("custom-key", headers.get(Constants.CURSEFORGE_API_KEY_HEADER));
    }

    @Test
    public void modrinthFileDownloadsIncludeMetadataHeaderWhenProvided() {
        Headers headers = Download.buildHeadersForUrl(
            "https://cdn.modrinth.com/data/project/versions/version/example.jar",
            Collections.emptyMap(),
            new ModrinthDownloadMetadata(ModrinthDownloadMetadata.Reason.MODPACK, "1.20.1", "fabric"));

        assertEquals("{\"reason\":\"modpack\",\"game_version\":\"1.20.1\",\"loader\":\"fabric\"}",
            headers.get(Constants.MODRINTH_DOWNLOAD_METADATA_HEADER));
    }

    @Test
    public void modrinthMetadataHeaderIsOnlyAddedForModrinthCdnDownloads() {
        Headers headers = Download.buildHeadersForUrl(
            "https://api.modrinth.com/v2/project/example",
            Collections.emptyMap(),
            new ModrinthDownloadMetadata(ModrinthDownloadMetadata.Reason.STANDALONE, "1.20.1", "fabric"));

        assertNull(headers.get(Constants.MODRINTH_DOWNLOAD_METADATA_HEADER));
    }

    @Test
    public void explicitModrinthMetadataHeaderIsPreserved() {
        Map<String, String> existingHeaders = new HashMap<>();
        existingHeaders.put("Modrinth-Download-Meta", "custom-meta");

        Headers headers = Download.buildHeadersForUrl(
            "https://cdn.modrinth.com/data/project/versions/version/example.jar",
            existingHeaders,
            new ModrinthDownloadMetadata(ModrinthDownloadMetadata.Reason.UPDATE, "1.20.1", "fabric"));

        assertEquals("custom-meta", headers.get(Constants.MODRINTH_DOWNLOAD_METADATA_HEADER));
    }
}
