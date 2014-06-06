/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.utils;

public final class HTMLifier {

    private String text;

    private HTMLifier(String text) {
        this.text = text;
    }

    public static HTMLifier wrap(String text) {
        return new HTMLifier(text);
    }

    public HTMLifier center() {
        this.text = "<center>" + this.text + "</center>";
        return this;
    }

    public HTMLifier addHTMLTags() {
        this.text = "<html>" + this.text + "</html>";
        return this;
    }

    public HTMLifier font(String color) {
        this.text = "<font color=" + color + ">" + this.text + "</font>";
        return this;
    }

    public HTMLifier bold() {
        this.text = "<b>" + this.text + "</b>";
        return this;
    }

    @Override
    public String toString() {
        return this.text;
    }

}
