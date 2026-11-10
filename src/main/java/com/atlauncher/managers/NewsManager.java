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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.rx3.Rx3Apollo;
import com.atlauncher.data.NewsItem;
import com.atlauncher.data.NewsSource;
import com.atlauncher.graphql.GetMinecraftNewsQuery;
import com.atlauncher.graphql.GetNewsQuery;
import com.atlauncher.network.GraphqlClient;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.Subject;

public class NewsManager {
    private static final Subject<Boolean> REFRESH = PublishSubject.<Boolean>create().toSerialized();

    public static Observable<Boolean> getRefreshEvents() {
        return REFRESH.hide();
    }

    /** Fetch one source independently; Apollo cancels the request when its subscription is disposed. */
    public static Single<List<NewsItem>> fetchNews(NewsSource source, int first, int skip) {
        if (first < 1 || first > 100 || skip < 0) {
            return Single.error(new IllegalArgumentException("Invalid news pagination"));
        }

        return Single.defer(() -> {
            HttpCachePolicy.Policy policy = new HttpCachePolicy.Policy(HttpCachePolicy.FetchStrategy.NETWORK_FIRST,
                    30, TimeUnit.MINUTES, false);
            if (source == NewsSource.MINECRAFT) {
                return Rx3Apollo.from(GraphqlClient.apolloClient.query(new GetMinecraftNewsQuery(first, skip))
                        .toBuilder().httpCachePolicy(policy).build()).firstOrError().map(response -> {
                            GetMinecraftNewsQuery.Data data = requireData(response);
                            return data.minecraftNews().stream().map(item -> new NewsItem(item.id(), item.title(),
                                    item.content(), item.createdAt(), source, item.url(), item.imageUrl()))
                                    .collect(Collectors.toList());
                        });
            }

            return Rx3Apollo.from(GraphqlClient.apolloClient.query(new GetNewsQuery(first, skip))
                    .toBuilder().httpCachePolicy(policy).build()).firstOrError().map(response -> {
                        GetNewsQuery.Data data = requireData(response);
                        return data.generalNews().stream().map(item -> new NewsItem(item.id(), item.title(),
                                item.content(), item.createdAt(), source, null, null)).collect(Collectors.toList());
                    });
        }).doOnError(error -> LogManager.logStackTrace("Error fetching " + source + " news", error));
    }

    private static <T> T requireData(Response<T> response) throws IOException {
        if (response.getErrors() != null && !response.getErrors().isEmpty()) {
            throw new IOException(response.getErrors().get(0).getMessage());
        }
        if (response.getData() == null) {
            throw new IOException("The news request returned no data");
        }
        return response.getData();
    }

    /** Refresh feeds that have already been opened without fetching unopened sources. */
    public static void loadNews() {
        REFRESH.onNext(true);
    }
}
