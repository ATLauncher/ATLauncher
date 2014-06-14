package com.atlauncher.reporter;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.utils.Utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GithubIssueReporter{
    public static final String GITHUB_API = "https://api.github.com";

    private GithubIssueReporter(){}

    public static void submit(String title, String body)
    throws IOException {
        String log = Utils.uploadLog();
        body = body + "\n\n" + times('-', 50) + "\n" + "Here is my log: " + log;
        HttpsURLConnection conn = (HttpsURLConnection) new URL(GITHUB_API + "/repos/ATLauncher/ATLauncher/issues").openConnection();
        conn.setRequestProperty("Authorization", "token " + Constants.GIT_REPORTER_AUTH);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.connect();
        conn.getOutputStream().write(new GithubIssue(title, body).toString().getBytes(StandardCharsets.UTF_8));
        conn.getOutputStream().flush();
        conn.getOutputStream().close();
        String line;
        StringBuilder builder = new StringBuilder();
        InputStream stream;
        try{
            stream = conn.getInputStream();
        } catch(Exception ex){
            ex.printStackTrace(System.err);
            stream = conn.getErrorStream();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while((line = reader.readLine()) != null){
            builder.append(line);
        }
    }

    private static String times(char c, int times){
        String s = "";
        for(int i = 0; i < times; i++){
            s += c;
        }
        return s;
    }
}