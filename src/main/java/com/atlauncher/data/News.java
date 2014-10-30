/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
 */
package com.atlauncher.data;

import com.atlauncher.annot.Json;

/**
 * News class contains a single news article.
 */
@Json
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
        return "<p id=\"newsHeader\">- <a href=\"" + this.link + "\">" + this.title + "</a> (" + this.comments + " "
                + (this.comments == 1 ? "comment" : "comments") + ")</p>" + "<p id=\"newsBody\">" + this.content +
                "</p><br/>";
    }
}
