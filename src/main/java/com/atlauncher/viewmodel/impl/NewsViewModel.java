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
package com.atlauncher.viewmodel.impl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy.FetchStrategy;
import com.apollographql.apollo.exception.ApolloException;
import com.atlauncher.App;
import com.atlauncher.data.News;
import com.atlauncher.graphql.GetNewsQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.NewsManager;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.viewmodel.base.INewsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NewsViewModel implements INewsViewModel {
    private BehaviorSubject<String> newsHTML = BehaviorSubject.create();

    @Override
    public Observable<String> getNewsHTML() {
        return newsHTML.observeOn(SwingSchedulers.edt());
    }

    @Override
    public void reload() {
        if (ConfigManager.getConfigItem("useGraphql.news", false)) {
            GraphqlClient.apolloClient.query(new GetNewsQuery(10))
                .toBuilder()
                .httpCachePolicy(new HttpCachePolicy.Policy(FetchStrategy.CACHE_FIRST, 30, TimeUnit.MINUTES, false))
                .build()
                .enqueue(new ApolloCall.Callback<GetNewsQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<GetNewsQuery.Data> response) {
                        newsHTML.onNext(newsAsHTML(response.getData().generalNews()));
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        LogManager.logStackTrace("Error fetching news", e);
                        newsHTML.onNext(getNewsAsHTML());
                    }
                });
        } else {
            newsHTML.onNext(getNewsAsHTML());
        }
    }

    /**
     * Get the News for the Launcher in HTML for display on the news panel.
     *
     * @return The HTML for displaying on the News Panel
     */
    static String getNewsAsHTML() {
        StringBuilder news = new StringBuilder("<html>");

        for (News newsItem : NewsManager.getNews()) {
            news.append(newsItem.getHTML()).append("<hr/>");
        }

        // remove the last <hr/>
        news = new StringBuilder(news.substring(0, news.length() - 5));
        news.append("</html>");

        return news.toString();
    }

    /**
     * Takes a list of news items from GraphQL query and transforms into HTML.
     *
     * @return The HTML for displaying on the News Panel
     */
    static String newsAsHTML(List<GetNewsQuery.GeneralNew> newsItems) {
        StringBuilder news = new StringBuilder("<html>");
        SimpleDateFormat formatter = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

        for (GetNewsQuery.GeneralNew newsItem : newsItems) {
            news.append("<h2>" + newsItem.title() + " (" + formatter.format(newsItem.createdAt()) + ")</h2>" + "<p>"
                + newsItem.content() + "</p><hr/>");
        }

        // remove the last <hr/>
        news = new StringBuilder(news.substring(0, news.length() - 5));
        news.append("</html>");

        return news.toString();
    }
}
