/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.io.IOException;
import java.io.Reader;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Text extends HTMLEditorKit.ParserCallback {
    StringBuffer s;
    public boolean readyForNewline;

    public Html2Text() {
    }

    public void parse(Reader in) throws IOException {
        s = new StringBuffer();
        ParserDelegator delegator = new ParserDelegator();
        delegator.parse(in, this, Boolean.TRUE);
    }

    public void handleStartTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
        if (readyForNewline && (t == HTML.Tag.BR)) {
            s.append("\n");
            readyForNewline = false;
        }
    }

    public void handleText(char[] text, int pos) {
        s.append(text);
        readyForNewline = true;
    }

    public void handleSimpleTag(final HTML.Tag t, final MutableAttributeSet a, final int pos) {
        handleStartTag(t, a, pos);
    }

    public String getText() {
        return s.toString();
    }
}
