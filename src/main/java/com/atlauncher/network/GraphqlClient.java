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
package com.atlauncher.network;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.CustomTypeAdapter;
import com.apollographql.apollo.api.CustomTypeValue;
import com.apollographql.apollo.api.CustomTypeValue.GraphQLString;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy;
import com.apollographql.apollo.api.cache.http.HttpCachePolicy.FetchStrategy;
import com.apollographql.apollo.cache.http.ApolloHttpCache;
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore;
import com.apollographql.apollo.internal.batch.BatchConfig;
import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.graphql.type.CustomType;
import com.atlauncher.managers.LogManager;

public class GraphqlClient {

    static {

        CustomTypeAdapter<String> idCustomTypeAdapter = new CustomTypeAdapter<String>() {
            @Override
            public String decode(CustomTypeValue<?> value) {
                return value.value.toString();
            }

            @Override
            public CustomTypeValue<?> encode(String value) {
                return new GraphQLString(value);
            }
        };

        CustomTypeAdapter<Date> dateCustomTypeAdapter = new CustomTypeAdapter<Date>() {
            DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

            @Override
            public Date decode(CustomTypeValue<?> value) {
                try {
                    return iso8601Format.parse(value.value.toString());
                } catch (ParseException e) {
                    LogManager.logStackTrace(e);
                    return null;
                }
            }

            @Override
            public CustomTypeValue<?> encode(Date value) {
                return new GraphQLString(iso8601Format.format(value));
            }

        };

        DiskLruHttpCacheStore cacheStore = new DiskLruHttpCacheStore(FileSystem.APOLLO_CACHE.toFile(),
                100 * 1024 * 1024);

        apolloClient = ApolloClient.builder()
                .serverUrl(Constants.GRAPHQL_ENDPOINT)
                .addCustomTypeAdapter(CustomType.ID, idCustomTypeAdapter)
                .addCustomTypeAdapter(CustomType.DATETIME, dateCustomTypeAdapter)
                .okHttpClient(Network.GRAPHQL_CLIENT)
                .httpCache(new ApolloHttpCache(cacheStore))
                .defaultHttpCachePolicy(
                        new HttpCachePolicy.Policy(FetchStrategy.CACHE_FIRST, 1, TimeUnit.MINUTES, false))
                .batchingConfiguration(new BatchConfig(true, 500, 10))
                .build();
    }

    public final static ApolloClient apolloClient;
}
