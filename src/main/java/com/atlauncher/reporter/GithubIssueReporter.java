/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

package com.atlauncher.reporter;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.APIResponse;
import com.atlauncher.thread.PasteUpload;
import com.atlauncher.utils.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GithubIssueReporter {
    private GithubIssueReporter() {
    }

    public static void submit(String title, String body) throws Exception {
        if (App.settings != null && App.settings.enableLogs()) {
            body = body + "\n\n" + times('-', 50) + "\n" + "Here is my log: " + App.TASKPOOL.submit(new PasteUpload()
            ).get();
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("issue", new GithubIssue(title, body));

            try {
                APIResponse response = Gsons.DEFAULT.fromJson(Utils.sendAPICall("githubissue/", request), APIResponse
                        .class);
                if (!response.wasError() && response.getDataAsInt() != 0) {
                    LogManager.info("Exception reported to GitHub. Track/comment on the issue at " + response
                            .getDataAsString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String times(char c, int times) {
        String s = "";
        for (int i = 0; i < times; i++) {
            s += c;
        }
        return s;
    }
}