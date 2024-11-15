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
package com.atlauncher.utils;

import java.util.Collections;
import java.util.Set;

import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;

public class Markdown {
    private static class NoImageNodeRenderer extends CoreHtmlNodeRenderer {
        public NoImageNodeRenderer(HtmlNodeRendererContext context) {
            super(context);
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            // Return node types that should be rendered by this renderer
            return Collections.singleton(Image.class);
        }

        @Override
        public void render(Node node) {
            // Do nothing for Image nodes
        }
    }

    public static String render(String text) {
        if (text == null) {
            return "";
        }

        Parser parser = Parser.builder().build();
        Node document = parser.parse(text);

        HtmlRenderer renderer = HtmlRenderer.builder()
                .sanitizeUrls(true)
                .escapeHtml(true)
                .nodeRendererFactory(NoImageNodeRenderer::new)
                .build();

        return renderer.render(document);
    }
}
