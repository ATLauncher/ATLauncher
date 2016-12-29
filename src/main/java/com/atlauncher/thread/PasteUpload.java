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

package com.atlauncher.thread;

import com.atlauncher.App;
import com.atlauncher.data.Constants;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public final class PasteUpload implements Callable<String> {
    @Override
    public String call() throws Exception {
        String log = App.settings.getLog().replace(System.getProperty("line.separator"), "\n");
        String urlParameters = "";
        urlParameters += "title=" + URLEncoder.encode(Constants.LAUNCHER_NAME + " - Log", "UTF-8") + "&";
        urlParameters += "language=" + URLEncoder.encode("text", "UTF-8") + "&";
        urlParameters += "private=" + URLEncoder.encode("1", "UTF-8") + "&";
        urlParameters += "text=" + URLEncoder.encode(log, "UTF-8");
        HttpURLConnection conn = (HttpURLConnection) new URL(Constants.PASTE_API_URL).openConnection();
        conn.setDoOutput(true);
        conn.connect();
        conn.getOutputStream().write(urlParameters.getBytes());
        conn.getOutputStream().flush();
        conn.getOutputStream().close();

        String line;
        StringBuilder builder = new StringBuilder();
        InputStream stream;
        try {
            stream = conn.getInputStream();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            stream = conn.getErrorStream();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }
}