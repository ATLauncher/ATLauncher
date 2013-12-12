/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

public class News {

    private String title;
    private int comments;
    private String link;
    private String content;

    public String toString() {
        return "<p id=\"newsHeader\">- <a href=\"" + this.link + "\">" + this.title + "</a> ("
                + this.comments + " " + (this.comments == 1 ? "comment" : "comments") + ")</p>"
                + "<p id=\"newsBody\">" + this.content + "</p><br/>";
    }
}
