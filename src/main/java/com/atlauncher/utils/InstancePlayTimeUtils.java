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
package com.atlauncher.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.data.Instance;
import com.atlauncher.graphql.AddPackActionMutation;
import com.atlauncher.graphql.AddPackTimePlayedMutation;
import com.atlauncher.graphql.type.AddPackActionInput;
import com.atlauncher.graphql.type.AddPackTimePlayedInput;
import com.atlauncher.graphql.type.PackLogAction;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.GraphqlClient;

/**
 * @since 2023 / 11 / 20
 */
public final class InstancePlayTimeUtils {

    public static void addPlay(Instance instance, String version) {
        if (ConfigManager.getConfigItem("useGraphql.packActions", false) == true) {
            GraphqlClient
                .mutateAndWait(
                    new AddPackActionMutation(AddPackActionInput.builder().packId(Integer.toString(
                            instance.getPack().id))
                        .version(version).action(PackLogAction.PLAY).build()));
        } else {
            Map<String, Object> request = new HashMap<>();

            request.put("version", version);

            try {
                Utils.sendAPICall("pack/" + instance.getPack().getSafeName() + "/play", request);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

    }

    public static void addTimePlayed(Instance instance, int time, String version) {
        if (ConfigManager.getConfigItem("useGraphql.packActions", false) == true) {
            GraphqlClient
                .mutateAndWait(
                    new AddPackTimePlayedMutation(AddPackTimePlayedInput.builder().packId(Integer.toString(
                        instance.getPack().id)).version(version).time(time).build()));
        } else {
            Map<String, Object> request = new HashMap<>();

            request.put("version", version);
            request.put("time", time);

            try {
                Utils.sendAPICall("pack/" + instance.getPack().getSafeName() + "/timeplayed/", request);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }
}
