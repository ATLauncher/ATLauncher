/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

/**
 * News class contains a single news article.
 */
public class News {
    /**
     * The title of this news article.
     */
    private String title;

    /**
     * The number of comments on this news article.
     */
    private int comments;

    /**
     * The link to this news article.
     */
    private String link;

    /**
     * The content of this news article.
     */
    private String content;

    /**
     * Gets the HTML of this object.
     */
    public String getHTML() {
        return "<p id=\"newsHeader\">- <a href=\"" + this.link + "\">" + this.title + "</a> ("
                + this.comments + " " + (this.comments == 1 ? "comment" : "comments") + ")</p>"
                + "<p id=\"newsBody\">" + this.content + "</p><br/>";
    }
}
