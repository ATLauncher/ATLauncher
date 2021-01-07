/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.thread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

import com.atlauncher.App;
import com.atlauncher.constants.Constants;
import com.atlauncher.managers.LogManager;

public final class PasteUpload implements Callable<String> {
    @Override
    public String call() {
        String log = App.console.getLog().replace(System.getProperty("line.separator"), "\n");
        String urlParameters = "";
        try {
            urlParameters += "title=" + URLEncoder.encode(Constants.LAUNCHER_NAME + " - Log", "UTF-8") + "&";
            urlParameters += "language=" + URLEncoder.encode("text", "UTF-8") + "&";
            urlParameters += "private=" + URLEncoder.encode("1", "UTF-8") + "&";
            urlParameters += "text=" + URLEncoder.encode(log, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LogManager.logStackTrace("Unsupported encoding", e);
            return "Unsupported encoding";
        }
        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) new URL(Constants.PASTE_API_URL).openConnection();
        } catch (MalformedURLException e) {
            LogManager.logStackTrace("Malformed paste API URL", e);
            return "Malformed paste API URL";
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to connect to paste API", e);
            return "Failed to connect to paste API";
        }
        conn.setDoOutput(true);
        try {
            conn.connect();
            conn.getOutputStream().write(urlParameters.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to send data to paste API", e);
            return "Failed to send data to paste API";
        }

        StringBuilder builder = new StringBuilder();
        InputStream stream;
        try {
            stream = conn.getInputStream();
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to receive response from paste API", e);
            stream = conn.getErrorStream();
            if (stream == null) {
                LogManager.error("No error message returned from paste API");
                return "No error message returned from paste API";
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to read error data", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to close error reader", e);
            }
        }
        return builder.toString();
    }
}
