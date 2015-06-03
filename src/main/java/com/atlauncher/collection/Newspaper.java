package com.atlauncher.collection;

import com.atlauncher.data.News;

import java.util.LinkedList;

public final class Newspaper
extends LinkedList<News>{
    @Override
    public String toString(){
        String news = "<html>";

        for (News newsItem : this) {
            news += newsItem.getHTML() + "<hr/>";
        }

        news = news.substring(0, news.length() - 5);
        news += "</html>";
        return news;
    }
}