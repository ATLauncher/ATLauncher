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
package com.atlauncher.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.exception.ApolloException;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.AbstractNews;
import com.atlauncher.data.News;
import com.atlauncher.graphql.GetNewsQuery;
import com.atlauncher.network.GraphqlClient;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NewsManager {
    static final BehaviorSubject<List<AbstractNews>> NEWS =
        BehaviorSubject.createDefault(Collections.emptyList());

    /**
     * Get the News for the Launcher
     *
     * @return The News items
     */
    public static Observable<List<AbstractNews>> getNews() {
        return NEWS;
    }

    /**
     * Loads the languages for use in the Launcher
     */
    public static void loadFileNews() {
        PerformanceManager.start();
        LogManager.debug("Loading news");
        try {
            java.lang.reflect.Type type = new TypeToken<List<News>>() {
            }.getType();
            File fileDir = FileSystem.JSON.resolve("newnews.json").toFile();
            BufferedReader in = new BufferedReader(
                new InputStreamReader(Files.newInputStream(fileDir.toPath()), StandardCharsets.UTF_8));

            List<News> fileNews = Gsons.DEFAULT.fromJson(in, type);
            in.close();

            // Map the [News] to [AbstractNews]
            NEWS.onNext(fileNews.stream().map(AbstractNews::new).collect(Collectors.toList()));
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading news");
        PerformanceManager.end();
    }

    /**
     * Attempt to load news from the network
     */
    public static void loadNetworkNews() {
        if (ConfigManager.getConfigItem("useGraphql.news", false)) {
            GraphqlClient.apolloClient.query(new GetNewsQuery(10))
                .toBuilder()
                .httpCachePolicy(new HttpCachePolicy.Policy(HttpCachePolicy.FetchStrategy.CACHE_FIRST, 30, TimeUnit.MINUTES, false))
                .build()
                .enqueue(new ApolloCall.Callback<GetNewsQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetNewsQuery.Data> response) {
                        GetNewsQuery.Data data = response.getData();
                        if (data == null) return;
                        List<GetNewsQuery.GeneralNew> networkNews = data.generalNews();
                        NEWS.onNext(networkNews.stream().map(AbstractNews::new).collect(Collectors.toList()));
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LogManager.logStackTrace("Error fetching news", e);
                    }
                });
        }
    }
}
