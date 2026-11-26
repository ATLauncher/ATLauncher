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
package com.atlauncher.data.minecraft;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.atlauncher.Gsons;
import com.google.gson.JsonObject;

class MinecraftVersionUpdatesTest {
    @Test
    void appliesMojangFieldsAndArgumentsWhileKeepingLoaderAndUserOverrides() {
        JsonObject previous = json("{'id':'1.0','mainClass':'vanilla.Old','complianceLevel':0,"
            + "'arguments':{'game':['--old','value'],'jvm':['-Dold=true']},"
            + "'javaVersion':{'component':'alpha','majorVersion':8},'assetIndex':{'id':'old'},"
            + "'downloads':{'client':{'url':'old'},'server':{'url':'old-server'}},"
            + "'logging':{'client':{'file':{'id':'old'}}},'libraries':[]}");
        JsonObject latest = json("{'id':'1.0','mainClass':'vanilla.New','complianceLevel':1,"
            + "'arguments':{'game':['--new','value'],'jvm':['-Dnew=true','-Dadded=true']},"
            + "'javaVersion':{'component':'beta','majorVersion':21},'assetIndex':{'id':'new'},"
            + "'downloads':{'client':{'url':'new'},'server':{'url':'new-server'}},"
            + "'logging':{'client':{'file':{'id':'new'}}},'libraries':[]}");
        JsonObject installed = Gsons.DEFAULT.toJsonTree(
            Gsons.DEFAULT.fromJson(previous, MinecraftVersion.class)).getAsJsonObject();
        installed.addProperty("mainClass", "loader.Main");
        installed.getAsJsonObject("arguments").getAsJsonArray("game").add("--loader-arg");
        installed.getAsJsonObject("arguments").getAsJsonArray("jvm").add("-Dcustom=true");
        installed.getAsJsonObject("downloads").getAsJsonObject("server").addProperty("url", "manual-server");
        installed.add("manualSetting", json("{'keep':true}"));

        List<String> messages = new ArrayList<>();
        JsonObject changes = MinecraftVersionUpdates.changes(installed, previous, latest, true, name -> true,
            messages::add);
        MinecraftVersionUpdates.apply(installed, changes);

        assertEquals("loader.Main", installed.get("mainClass").getAsString());
        assertEquals(json("{'game':['--new','value','--loader-arg'],'jvm':['-Dnew=true','-Dadded=true','-Dcustom=true']}"),
            installed.get("arguments"));
        assertEquals(21, installed.getAsJsonObject("javaVersion").get("majorVersion").getAsInt());
        assertEquals("new", installed.getAsJsonObject("assetIndex").get("id").getAsString());
        assertEquals("new", installed.getAsJsonObject("downloads").getAsJsonObject("client").get("url").getAsString());
        assertEquals("manual-server",
            installed.getAsJsonObject("downloads").getAsJsonObject("server").get("url").getAsString());
        assertEquals("new", installed.getAsJsonObject("logging").getAsJsonObject("client").getAsJsonObject("file")
            .get("id").getAsString());
        assertEquals(json("{'keep':true}"), installed.get("manualSetting"));
        assertEquals(Arrays.asList("Updated complianceLevel", "Updated javaVersion.component",
            "Updated javaVersion.majorVersion", "Updated assetIndex.id", "Updated logging.client.file.id",
            "Updated downloads.client.url", "Updated arguments.game", "Updated arguments.jvm"), messages);
    }

    @Test
    void mergesNewAndWithdrawnMojangLibrariesWithoutRestoringManualRemovals() {
        JsonObject old = json("{'libraries':[{'name':'test:withdrawn:1'}, {'name':'test:deleted:1'},"
            + "{'name':'test:updated:1'}]}");
        JsonObject latest = json("{'libraries':[{'name':'test:added:1'}, {'name':'test:filtered:1'}, {'name':'test:deleted:2'},"
            + "{'name':'test:updated:2'}]}");
        JsonObject installed = json("{'libraries':[{'name':'test:withdrawn:1'}, {'name':'test:updated:1'},"
            + "{'name':'test:custom:1'}]}");

        List<String> messages = new ArrayList<>();
        JsonObject changes = MinecraftVersionUpdates.changes(installed, old, latest, false,
            name -> !name.equals("test:filtered:1"), messages::add);

        assertEquals(json("{'libraries':[{'name':'test:updated:2'}, {'name':'test:custom:1'},"
            + "{'name':'test:added:1'}]}").get("libraries"), changes.get("libraries"));
        assertEquals(Arrays.asList("Removed library test:withdrawn:1", "Updated library test:updated:1 to test:updated:2",
            "Added library test:added:1"), messages);
    }

    @Test
    void preservesUnknownArgumentOwnershipWhenMigratingWithoutPriorDefinition() {
        JsonObject installed = json("{'mainClass':'custom.Main','arguments':{'jvm':['-Dcustom=true']},'libraries':[]}");
        JsonObject latest = json("{'mainClass':'vanilla.Main','arguments':{'jvm':['-Dnew=true']},"
            + "'javaVersion':{'majorVersion':21},'libraries':[]}");

        JsonObject changes = MinecraftVersionUpdates.changes(installed, null, latest, true, name -> false, message -> {});

        assertFalse(changes.has("mainClass"));
        assertFalse(changes.has("arguments"));
        assertEquals(21, changes.getAsJsonObject("javaVersion").get("majorVersion").getAsInt());
    }

    private static JsonObject json(String value) {
        return Gsons.DEFAULT.fromJson(value, JsonObject.class);
    }
}
