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

import java.util.List;
import java.util.Optional;

import com.atlauncher.data.AbstractNews;
import com.atlauncher.managers.NewsManager;
import com.atlauncher.viewmodel.base.INewsViewModel;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.core.Observable;

public class NewsViewModel implements INewsViewModel {
    private final Observable<Optional<String>> newsHTML = NewsManager
            .getNews()
            .map(NewsViewModel::newsAsHTML)
            .observeOn(SwingSchedulers.edt());

    @Override
    public Observable<Optional<String>> getNewsHTML() {
        return newsHTML;
    }

    /**
     * Takes a list of news items from GraphQL query and transforms into HTML.
     *
     * @return An optional value containing the HTML for displaying on the News Panel
     */
    static Optional<String> newsAsHTML(List<AbstractNews> newsItems) {
        if (newsItems == null || newsItems.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder news = new StringBuilder("<html>");

        for (AbstractNews newsItem : newsItems) {
            news.append(newsItem.htmlEntry).append("<hr/>");
        }

        // remove the last <hr/>
        news = new StringBuilder(news.substring(0, news.length() - 5));
        news.append("</html>");

        return Optional.of(news.toString());
    }
}
