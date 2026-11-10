/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2026 ATLauncher
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.atlauncher.data.NewsFeedState;
import com.atlauncher.data.NewsItem;
import com.atlauncher.data.NewsSource;

import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class NewsViewModelTest {
    private static final class Request {
        final NewsSource source;
        final int first;
        final int skip;
        final SingleSubject<List<NewsItem>> result = SingleSubject.create();

        Request(NewsSource source, int first, int skip) {
            this.source = source;
            this.first = first;
            this.skip = skip;
        }
    }

    private final List<Request> requests = new ArrayList<>();
    private final PublishSubject<Boolean> refreshes = PublishSubject.create();
    private NewsViewModel viewModel;
    private TestObserver<NewsFeedState> launcher;
    private TestObserver<NewsFeedState> minecraft;

    @BeforeEach
    public void setUp() {
        viewModel = new NewsViewModel((source, first, skip) -> {
            Request request = new Request(source, first, skip);
            requests.add(request);
            return request.result;
        }, Schedulers.trampoline(), refreshes);
        launcher = viewModel.getNews(NewsSource.LAUNCHER).test();
        minecraft = viewModel.getNews(NewsSource.MINECRAFT).test();
    }

    private static NewsFeedState latest(TestObserver<NewsFeedState> observer) {
        List<NewsFeedState> values = observer.values();
        return values.get(values.size() - 1);
    }

    private static List<NewsItem> page(NewsSource source, int firstId, int count) {
        List<NewsItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String id = Integer.toString(firstId + i);
            items.add(new NewsItem(id, "News " + id, "Article " + id, new Date(1000), source, null, null));
        }
        return items;
    }

    @Test
    public void loadsSourcesOnlyWhenRequestedAndKeepsTheirStateIndependent() {
        assertTrue(requests.isEmpty());
        assertFalse(latest(launcher).initialized);
        assertFalse(latest(minecraft).initialized);

        viewModel.loadNews(NewsSource.LAUNCHER);
        assertEquals(1, requests.size());
        assertEquals(NewsSource.LAUNCHER, requests.get(0).source);
        assertEquals(12, requests.get(0).first);
        assertEquals(0, requests.get(0).skip);
        assertTrue(latest(launcher).loading);
        assertFalse(latest(minecraft).loading);

        requests.get(0).result.onSuccess(page(NewsSource.LAUNCHER, 0, 12));
        viewModel.loadNews(NewsSource.MINECRAFT);
        assertEquals(2, requests.size());
        assertEquals(NewsSource.MINECRAFT, requests.get(1).source);
        assertTrue(latest(minecraft).loading);
        assertEquals(12, latest(launcher).items.size());

        requests.get(1).result.onSuccess(page(NewsSource.MINECRAFT, 0, 2));
        viewModel.loadNews(NewsSource.LAUNCHER);
        viewModel.loadNews(NewsSource.MINECRAFT);
        assertEquals(2, requests.size());
        assertEquals(12, latest(launcher).items.size());
        assertEquals(2, latest(minecraft).items.size());
    }

    @Test
    public void suppressesDuplicateRequestsWhileAPageIsLoading() {
        viewModel.loadNews(NewsSource.MINECRAFT);
        viewModel.loadNews(NewsSource.MINECRAFT);
        viewModel.loadMore(NewsSource.MINECRAFT);
        viewModel.retry(NewsSource.MINECRAFT);
        assertEquals(1, requests.size());

        requests.get(0).result.onSuccess(page(NewsSource.MINECRAFT, 0, 12));
        viewModel.loadMore(NewsSource.MINECRAFT);
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertEquals(2, requests.size());
        assertEquals(12, requests.get(1).skip);
    }

    @Test
    public void retriesTheFailedOffsetWithoutDiscardingLoadedArticles() {
        viewModel.loadNews(NewsSource.MINECRAFT);
        requests.get(0).result.onSuccess(page(NewsSource.MINECRAFT, 0, 12));
        viewModel.loadMore(NewsSource.MINECRAFT);
        requests.get(1).result.onError(new IOException("Temporary failure"));

        NewsFeedState failed = latest(minecraft);
        assertEquals(12, failed.items.size());
        assertTrue(failed.initialized);
        assertFalse(failed.loading);
        assertNotNull(failed.error);
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertEquals(2, requests.size());

        viewModel.retry(NewsSource.MINECRAFT);
        assertEquals(12, requests.get(2).skip);
        assertTrue(latest(minecraft).loading);
        assertNull(latest(minecraft).error);
        requests.get(2).result.onSuccess(page(NewsSource.MINECRAFT, 12, 3));
        assertEquals(15, latest(minecraft).items.size());
        assertFalse(latest(minecraft).hasMore);
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertEquals(3, requests.size());
    }

    @Test
    public void retriesInitialFailureAndTreatsAnEmptyPageAsAnInitializedFeed() {
        viewModel.loadNews(NewsSource.LAUNCHER);
        requests.get(0).result.onError(new IOException("Offline"));
        assertFalse(latest(launcher).initialized);
        viewModel.loadNews(NewsSource.LAUNCHER);
        assertEquals(1, requests.size());

        viewModel.retry(NewsSource.LAUNCHER);
        assertEquals(0, requests.get(1).skip);
        requests.get(1).result.onSuccess(Collections.emptyList());
        assertTrue(latest(launcher).initialized);
        assertFalse(latest(launcher).hasMore);
        assertNull(latest(launcher).error);
        viewModel.loadNews(NewsSource.LAUNCHER);
        viewModel.loadMore(NewsSource.LAUNCHER);
        assertEquals(2, requests.size());
    }

    @Test
    public void deduplicatesArticleIdsWhileAdvancingByTheFetchedPageCount() {
        viewModel.loadNews(NewsSource.MINECRAFT);
        requests.get(0).result.onSuccess(page(NewsSource.MINECRAFT, 0, 12));
        NewsFeedState initial = latest(minecraft);
        viewModel.loadMore(NewsSource.MINECRAFT);
        requests.get(1).result.onSuccess(page(NewsSource.MINECRAFT, 11, 12));

        assertEquals(23, latest(minecraft).items.size());
        assertTrue(latest(minecraft).hasMore);
        // The first emitted snapshot must stay unchanged as later pages arrive.
        assertEquals(12, initial.items.size());
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertEquals(24, requests.get(2).skip);
    }

    @Test
    public void refreshRetriesAnInitiallyFailedFeedWithoutLoadingUnopenedSources() {
        viewModel.loadNews(NewsSource.LAUNCHER);
        requests.get(0).result.onError(new IOException("Offline"));

        refreshes.onNext(true);

        assertEquals(2, requests.size());
        assertEquals(NewsSource.LAUNCHER, requests.get(1).source);
        assertEquals(0, requests.get(1).skip);
        assertTrue(latest(launcher).loading);
        assertNull(latest(launcher).error);
        assertFalse(latest(minecraft).initialized);
        assertFalse(latest(minecraft).loading);

        requests.get(1).result.onSuccess(page(NewsSource.LAUNCHER, 0, 2));
        assertTrue(latest(launcher).initialized);
        assertEquals(2, latest(launcher).items.size());
    }

    @Test
    public void refreshesLoadedSourcesWithoutFetchingTheUnopenedMinecraftFeed() {
        viewModel.loadNews(NewsSource.LAUNCHER);
        requests.get(0).result.onSuccess(page(NewsSource.LAUNCHER, 0, 12));
        refreshes.onNext(true);

        assertEquals(2, requests.size());
        assertEquals(NewsSource.LAUNCHER, requests.get(1).source);
        assertEquals(0, requests.get(1).skip);
        assertTrue(latest(launcher).loading);
        assertEquals(12, latest(launcher).items.size());
        assertFalse(latest(minecraft).initialized);

        requests.get(1).result.onSuccess(page(NewsSource.LAUNCHER, 100, 2));
        assertEquals(2, latest(launcher).items.size());
        assertEquals("100", latest(launcher).items.get(0).id);
        assertFalse(latest(launcher).hasMore);
    }

    @Test
    public void refreshCancelsAnOldPageAndKeepsItsResponseOutOfTheNewFeed() {
        viewModel.loadNews(NewsSource.MINECRAFT);
        requests.get(0).result.onSuccess(page(NewsSource.MINECRAFT, 0, 12));
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertTrue(requests.get(1).result.hasObservers());

        refreshes.onNext(true);
        assertFalse(requests.get(1).result.hasObservers());
        assertEquals(0, requests.get(2).skip);
        requests.get(1).result.onSuccess(page(NewsSource.MINECRAFT, 12, 12));
        assertTrue(latest(minecraft).loading);
        assertEquals(12, latest(minecraft).items.size());

        requests.get(2).result.onSuccess(page(NewsSource.MINECRAFT, 100, 12));
        assertEquals("100", latest(minecraft).items.get(0).id);
        assertEquals(12, latest(minecraft).items.size());
        viewModel.loadMore(NewsSource.MINECRAFT);
        assertEquals(12, requests.get(3).skip);
    }
}
