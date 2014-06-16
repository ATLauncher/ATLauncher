package com.atlauncher.reporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.thread.PasteUpload;
import com.atlauncher.utils.Utils;

public final class GithubIssueReporter {
    private GithubIssueReporter() {}

    public static void submit(String title, String body) throws Exception {
        body = body + "\n\n" + times('-', 50) + "\n" + "Here is my log: "
                + App.TASKPOOL.submit(new PasteUpload()).get();
        Map<String, Object> request = new HashMap<String, Object>();
        request.put("issue", new GithubIssue(title, body));

        try {
            Utils.sendAPICall("githubissue/", request);
        } catch (IOException e) {
            e.printStackTrace();
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