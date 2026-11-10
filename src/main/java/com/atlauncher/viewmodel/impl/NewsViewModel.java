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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.atlauncher.data.NewsFeedState;
import com.atlauncher.data.NewsItem;
import com.atlauncher.data.NewsSource;
import com.atlauncher.managers.NewsManager;
import com.atlauncher.viewmodel.base.INewsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class NewsViewModel implements INewsViewModel {
    public static final int PAGE_SIZE = 12;

    @FunctionalInterface
    public interface NewsLoader {
        Single<List<NewsItem>> fetch(NewsSource source, int first, int skip);
    }

    private static final class Feed {
        final NewsSource source;
        final Map<String, NewsItem> items = new LinkedHashMap<>();
        final BehaviorSubject<NewsFeedState> changes;
        int nextOffset;
        int generation;
        Disposable request;
        boolean attempted;
        boolean initialized;
        boolean loading;
        boolean hasMore = true;
        String error;

        Feed(NewsSource source) {
            this.source = source;
            changes = BehaviorSubject.createDefault(snapshot());
        }

        NewsFeedState snapshot() {
            return new NewsFeedState(source, new ArrayList<>(items.values()), initialized, loading, hasMore, error);
        }
    }

    private final Map<NewsSource, Feed> feeds = new EnumMap<>(NewsSource.class);
    private final NewsLoader loader;
    private final Scheduler notifications;

    public NewsViewModel() {
        this(NewsManager::fetchNews, SwingSchedulers.edt(), NewsManager.getRefreshEvents());
    }

    public NewsViewModel(NewsLoader loader) {
        this(loader, SwingSchedulers.edt());
    }

    NewsViewModel(NewsLoader loader, Scheduler notifications) {
        this(loader, notifications, Observable.never());
    }

    NewsViewModel(NewsLoader loader, Scheduler notifications, Observable<?> refreshes) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
        for (NewsSource source : NewsSource.values()) {
            feeds.put(source, new Feed(source));
        }
        refreshes.subscribe(ignored -> refresh());
    }

    @Override
    public Observable<NewsFeedState> getNews(NewsSource source) {
        return feeds.get(Objects.requireNonNull(source, "source")).changes.hide().observeOn(notifications);
    }

    @Override
    public void loadNews(NewsSource source) {
        request(feeds.get(Objects.requireNonNull(source, "source")), true, false);
    }

    @Override
    public void loadMore(NewsSource source) {
        request(feeds.get(Objects.requireNonNull(source, "source")), false, false);
    }

    @Override
    public void retry(NewsSource source) {
        request(feeds.get(Objects.requireNonNull(source, "source")), false, true);
    }

    private void request(Feed feed, boolean initialOnly, boolean retry) {
        final int offset;
        final int generation;
        synchronized (this) {
            if (feed.loading || !feed.hasMore || (initialOnly && feed.initialized)
                    || (feed.error != null && !retry) || (retry && feed.error == null)) {
                return;
            }
            feed.attempted = true;
            feed.loading = true;
            feed.error = null;
            offset = feed.nextOffset;
            generation = feed.generation;
            feed.changes.onNext(feed.snapshot());
        }

        Disposable request = Single.defer(() -> loader.fetch(feed.source, PAGE_SIZE, offset)).subscribe(page -> {
            synchronized (this) {
                if (generation != feed.generation) {
                    return;
                }
                // Advance by the server page size, not the deduplicated display count.
                feed.nextOffset = offset + page.size();
                if (offset == 0) {
                    feed.items.clear();
                }
                for (NewsItem item : page) {
                    feed.items.put(item.id, item);
                }
                feed.initialized = true;
                feed.loading = false;
                feed.hasMore = page.size() >= PAGE_SIZE;
                feed.error = null;
                feed.changes.onNext(feed.snapshot());
            }
        }, error -> {
            synchronized (this) {
                if (generation != feed.generation) {
                    return;
                }
                feed.loading = false;
                feed.error = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
                feed.changes.onNext(feed.snapshot());
            }
        });
        synchronized (this) {
            if (generation == feed.generation) {
                feed.request = request;
            } else {
                request.dispose();
            }
        }
    }

    private void refresh() {
        for (Feed feed : feeds.values()) {
            synchronized (this) {
                if (!feed.attempted) {
                    continue;
                }
                feed.generation++;
                if (feed.request != null) {
                    feed.request.dispose();
                    feed.request = null;
                }
                feed.nextOffset = 0;
                feed.loading = false;
                feed.hasMore = true;
                feed.error = null;
            }
            request(feed, false, false);
        }
    }
}
