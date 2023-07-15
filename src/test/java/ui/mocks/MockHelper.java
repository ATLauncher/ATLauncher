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
package ui.mocks;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BinaryBody;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.DownloadableFile;
import com.atlauncher.utils.Hashing;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MockHelper {
    public static Map<String, Path> mockedFilePaths = new HashMap<>();

    static {
        mockedFilePaths.put("newnews.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/newnews.json"));
        mockedFilePaths.put("runtimes.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/runtimes.json"));
        mockedFilePaths.put("java_runtimes.json",
                Paths.get("src/test/resources/mocks/download-nodecdn-net/java_runtimes.json"));
        mockedFilePaths.put("users.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/users.json"));
        mockedFilePaths.put("packsnew.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/packsnew.json"));
        mockedFilePaths.put("version.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/version.json"));
        mockedFilePaths.put("config.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/config.json"));
        mockedFilePaths.put("lwjgl.json", Paths.get("src/test/resources/mocks/download-nodecdn-net/lwjgl.json"));
        mockedFilePaths.put("minecraft_versions.json",
                Paths.get("src/test/resources/mocks/download-nodecdn-net/minecraft_versions.json"));
    }

    public static void mockFilesJson(ClientAndServer mockServer) {
        List<DownloadableFile> downloadableFiles = new ArrayList<>();

        try {
            DownloadableFile launcher = new DownloadableFile();
            launcher.name = "launcher";
            launcher.folder = "launcher";
            launcher.size = 0;
            launcher.sha1 = Constants.VERSION.toStringForLogging();
            downloadableFiles.add(launcher);

            DownloadableFile newNews = new DownloadableFile();
            newNews.name = "newnews.json";
            newNews.folder = "json";
            newNews.size = (int) Files.size(mockedFilePaths.get("newnews.json"));
            newNews.sha1 = Hashing.sha1(mockedFilePaths.get("newnews.json")).toString();
            downloadableFiles.add(newNews);

            DownloadableFile runtimes = new DownloadableFile();
            runtimes.name = "runtimes.json";
            runtimes.folder = "json";
            runtimes.size = (int) Files.size(mockedFilePaths.get("runtimes.json"));
            runtimes.sha1 = Hashing.sha1(mockedFilePaths.get("runtimes.json")).toString();
            downloadableFiles.add(runtimes);

            DownloadableFile users = new DownloadableFile();
            users.name = "users.json";
            users.folder = "json";
            users.size = (int) Files.size(mockedFilePaths.get("users.json"));
            users.sha1 = Hashing.sha1(mockedFilePaths.get("users.json")).toString();
            downloadableFiles.add(users);

            DownloadableFile packsNew = new DownloadableFile();
            packsNew.name = "packsnew.json";
            packsNew.folder = "json";
            packsNew.size = (int) Files.size(mockedFilePaths.get("packsnew.json"));
            packsNew.sha1 = Hashing.sha1(mockedFilePaths.get("packsnew.json")).toString();
            downloadableFiles.add(packsNew);

            DownloadableFile version = new DownloadableFile();
            version.name = "version.json";
            version.folder = "json";
            version.size = (int) Files.size(mockedFilePaths.get("version.json"));
            version.sha1 = Hashing.sha1(mockedFilePaths.get("version.json")).toString();
            downloadableFiles.add(version);

            DownloadableFile config = new DownloadableFile();
            config.name = "config.json";
            config.folder = "json";
            config.size = (int) Files.size(mockedFilePaths.get("config.json"));
            config.sha1 = Hashing.sha1(mockedFilePaths.get("config.json")).toString();
            downloadableFiles.add(config);

            DownloadableFile lwjgl = new DownloadableFile();
            lwjgl.name = "lwjgl.json";
            lwjgl.folder = "json";
            lwjgl.size = (int) Files.size(mockedFilePaths.get("lwjgl.json"));
            lwjgl.sha1 = Hashing.sha1(mockedFilePaths.get("lwjgl.json")).toString();
            downloadableFiles.add(lwjgl);

            DownloadableFile minecraftVersions = new DownloadableFile();
            minecraftVersions.name = "minecraft_versions.json";
            minecraftVersions.folder = "json";
            minecraftVersions.size = (int) Files.size(mockedFilePaths.get("minecraft_versions.json"));
            minecraftVersions.sha1 = Hashing.sha1(mockedFilePaths.get("minecraft_versions.json")).toString();
            downloadableFiles.add(minecraftVersions);

            DownloadableFile javaRuntimes = new DownloadableFile();
            javaRuntimes.name = "java_runtimes.json";
            javaRuntimes.folder = "json";
            javaRuntimes.size = (int) Files.size(mockedFilePaths.get("java_runtimes.json"));
            javaRuntimes.sha1 = Hashing.sha1(mockedFilePaths.get("java_runtimes.json")).toString();
            downloadableFiles.add(javaRuntimes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mockServer.when(HttpRequest.request().withMethod("GET").withPath("/containers/atl/launcher/json/files.json"))
                .respond(HttpResponse.response().withStatusCode(200).withHeader("Content-Type", "application/json")
                        .withBody(Gsons.DEFAULT.toJson(downloadableFiles)));
    }

    public static void mockFileResponse(ClientAndServer mockServer, String string) {
        mockCdnJson(mockServer, "GET", "/containers/atl/launcher/json/" + string, string);
    }

    public static void mockNoResponseSuccess(ClientAndServer mockServer, String method, String host, String path) {
        mockServer.when(HttpRequest.request().withMethod(method).withHeader("Host", host).withPath(path))
                .respond(HttpResponse.response().withStatusCode(200).withHeader("Content-Type", "application/json"));
    }

    public static void mockCdnJson(ClientAndServer mockServer, String method, String path, String responseFile) {
        mock(mockServer, method, Constants.BASE_CDN_DOMAIN, path, responseFile, ResponseType.JSON);
    }

    public static void mockJson(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.JSON);
    }

    public static void mockMojangLogin(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.MOJANG_LOGIN);
    }

    public static void mockPng(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.PNG);
    }

    public static void mockJar(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.JAR);
    }

    public static void mockXml(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.XML);
    }

    public static void mockTxt(ClientAndServer mockServer, String method, String host, String path,
            String responseFile) {
        mock(mockServer, method, host, path, responseFile, ResponseType.TXT);
    }

    public static void mock(ClientAndServer mockServer, String method, String host, String path, String responseFile,
            ResponseType responseType) {
        try {
            Path filePath = Paths
                    .get(String.format("src/test/resources/mocks/%s/%s", host.replace(".", "-"), responseFile));

            ForwardChainExpectation expectation = mockServer
                    .when(HttpRequest.request().withMethod(method).withHeader("Host", host).withPath(path));

            switch (responseType) {
                case MOJANG_LOGIN:
                    expectation.respond(request -> {
                        JsonObject requestBody = JsonParser.parseString(request.getBodyAsString()).getAsJsonObject();
                        String clientToken = requestBody.get("clientToken").getAsString();

                        return HttpResponse.response().withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8)
                                        .replace("{{CLIENT_TOKEN}}", clientToken));
                    });
                    break;
                case PNG:
                case XML:
                case JAR:
                case TXT:
                    expectation.respond(HttpResponse.response().withStatusCode(200)
                            .withHeader("Content-Type", getContentType(responseType))
                            .withHeader("Content-Disposition",
                                    "form-data; name=\"" + responseFile + "\"; filename=\"" + responseFile + "\"")
                            .withBody(BinaryBody.binary(Files.readAllBytes(filePath))));
                    break;
                case JSON:
                default:
                    expectation.respond(
                            HttpResponse.response().withStatusCode(200).withHeader("Content-Type", "application/json")
                                    .withBody(new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8)));
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getContentType(ResponseType responseType) {
        switch (responseType) {
            case JAR:
                return "application/java-archive";
            case XML:
                return "application/xml";
            case TXT:
                return "plain/text";
            case PNG:
                return "image/png";
            case JSON:
            default:
                return "application/json";
        }
    }
}
