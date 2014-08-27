package com.atlauncher.reporter;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Settings;
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
                APIResponse response = Settings.gson.fromJson(Utils.sendAPICall("githubissue/", request),
                        APIResponse.class);
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