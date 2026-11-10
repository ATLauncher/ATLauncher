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
package com.atlauncher.gui.tabs.news;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.DefaultCaret;
import javax.swing.text.View;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.data.NewsItem;
import com.atlauncher.data.NewsSource;
import com.atlauncher.managers.NewsImageManager;
import com.atlauncher.utils.OS;
import com.gitlab.doomsdayrs.lib.rxswing.schedulers.SwingSchedulers;

import io.reactivex.rxjava3.disposables.Disposable;

final class NewsArticleCard extends JPanel {
    NewsArticleCard(NewsItem item, Consumer<Disposable> register) {
        super(new BorderLayout(NewsStyle.scale(16), 0));
        setOpaque(false);
        setName("newsArticle-" + item.id);
        int padding = NewsStyle.scale(item.source == NewsSource.MINECRAFT ? 14 : 18);
        setBorder(BorderFactory.createEmptyBorder(padding, padding + NewsStyle.scale(2), padding + 1,
            padding + NewsStyle.scale(2)));

        JPanel content = NewsStyle.stack();
        JPanel metadata = NewsStyle.panel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        String source = item.source == NewsSource.MINECRAFT ? "Minecraft" : "ATLauncher";
        NewsStyle.add(metadata, NewsStyle.label(source, 11, true, NewsStyle.accent()));
        String pattern = App.settings == null ? "d MMM yyyy" : App.settings.dateFormat;
        metadata.add(NewsStyle.label("   \u00b7   " + new SimpleDateFormat(pattern).format(item.createdAt), 11, false,
            NewsStyle.muted()));
        metadata.setMaximumSize(new Dimension(Integer.MAX_VALUE, metadata.getPreferredSize().height));
        NewsStyle.add(content, metadata);
        NewsStyle.gap(content, item.source == NewsSource.MINECRAFT ? 7 : 9);
        NewsStyle.add(content, new WrappedText(item.title, item.source == NewsSource.MINECRAFT ? 18 : 21, true,
            NewsStyle.foreground(), item.source == NewsSource.MINECRAFT ? 600 : 910));

        if (item.source == NewsSource.MINECRAFT) {
            if (!item.imageUrl.isEmpty()) {
                JPanel imageHolder = NewsStyle.panel(new GridBagLayout());
                imageHolder.add(new Thumbnail(item, register));
                add(imageHolder, BorderLayout.WEST);
            }
            String summary = item.content.replaceAll("\\s+", " ").trim();
            if (!summary.isEmpty() && !summary.equalsIgnoreCase(item.title.trim())) {
                NewsStyle.gap(content, 4);
                if (summary.length() > 165) {
                    summary = summary.substring(0, 162) + "...";
                }
                NewsStyle.add(content, new WrappedText(summary, 12, false, NewsStyle.muted(), 650));
            }
            JButton read = new ReadButton();
            read.setEnabled(!item.url.isEmpty());
            read.addActionListener(event -> OS.openWebBrowser(item.url));
            JPanel action = NewsStyle.panel(new GridBagLayout());
            action.add(read);
            add(action, BorderLayout.EAST);
        } else {
            NewsStyle.gap(content, 13);
            NewsStyle.add(content, new ArticleBody(item.content));
        }
        add(content, BorderLayout.CENTER);

        if (item.source == NewsSource.MINECRAFT) {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem copyLink = new JMenuItem(GetText.tr("Copy Link"));
            copyLink.setEnabled(!item.url.isEmpty());
            copyLink.addActionListener(event -> OS.copyToClipboard(item.url));
            menu.add(copyLink);
            installPopupMenu(this, menu);
        }
    }

    private static void installPopupMenu(JComponent component, JPopupMenu menu) {
        component.setComponentPopupMenu(menu);
        for (Component child : component.getComponents()) {
            if (child instanceof JComponent) {
                installPopupMenu((JComponent) child, menu);
            }
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D copy = (Graphics2D) graphics.create();
        copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        copy.setColor(NewsStyle.card());
        copy.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, NewsStyle.scale(10), NewsStyle.scale(10));
        copy.setColor(NewsStyle.border());
        copy.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, NewsStyle.scale(10), NewsStyle.scale(10));
        copy.dispose();
    }

    private static class WrappedText extends JTextArea {
        private final int preferredWidth;

        WrappedText(String value, int size, boolean bold, Color color, int preferredWidth) {
            super(value);
            this.preferredWidth = NewsStyle.scale(preferredWidth);
            setFont(NewsStyle.font(size, bold));
            setForeground(color);
            setOpaque(false);
            setEditable(false);
            setFocusable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(BorderFactory.createEmptyBorder());
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    revalidate();
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            if (getUI() == null) {
                return super.getPreferredSize();
            }
            int width = getWidth() > 0 ? getWidth() : Math.max(1, preferredWidth);
            Insets insets = getInsets();
            View root = getUI().getRootView(this);
            root.setSize(Math.max(1, width - insets.left - insets.right), Float.MAX_VALUE);
            return new Dimension(preferredWidth,
                (int) Math.ceil(root.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom + 2);
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, getPreferredSize().height);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    private static class ArticleBody extends JEditorPane {
        ArticleBody(String html) {
            setEditable(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            HTMLEditorKit kit = new HTMLEditorKit();
            StyleSheet css = new StyleSheet();
            css.addRule("body { font-family: '" + NewsStyle.font(13, false).getFamily() + "'; font-size: "
                + NewsStyle.scale(13) + "px; color: " + hex(NewsStyle.body()) + "; margin: 0; }");
            css.addRule("p { margin-top: 0; margin-bottom: " + NewsStyle.scale(10) + "px; }");
            css.addRule("a { color: " + hex(NewsStyle.accent()) + "; }");
            kit.setStyleSheet(css);
            setEditorKit(kit);
            ((DefaultCaret) getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
            setText("<html><body>" + html + "</body></html>");
            setCaretPosition(0);
            addHyperlinkListener(event -> {
                if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED && event.getURL() != null) {
                    OS.openWebBrowser(event.getURL());
                }
            });
            JPopupMenu menu = new JPopupMenu();
            JMenuItem copy = new JMenuItem(GetText.tr("Copy"));
            copy.addActionListener(event -> copy());
            menu.add(copy);
            setComponentPopupMenu(menu);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    revalidate();
                }
            });
        }

        private static String hex(Color color) {
            return String.format("#%06x", color.getRGB() & 0xffffff);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = getWidth() > 0 ? getWidth() : NewsStyle.scale(910);
            Insets insets = getInsets();
            View root = getUI().getRootView(this);
            root.setSize(Math.max(1, width - insets.left - insets.right), Float.MAX_VALUE);
            return new Dimension(width,
                (int) Math.ceil(root.getPreferredSpan(View.Y_AXIS)) + insets.top + insets.bottom + NewsStyle.scale(4));
        }

        @Override
        public Dimension getMinimumSize() {
            return new Dimension(0, getPreferredSize().height);
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(NewsStyle.scale(910), getPreferredSize().height);
        }
    }

    private static class ReadButton extends JButton {
        ReadButton() {
            super(GetText.tr("Read article"));
            setFont(NewsStyle.font(12, true));
            setForeground(NewsStyle.accent());
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(NewsStyle.scale(9), NewsStyle.scale(10), NewsStyle.scale(9),
                NewsStyle.scale(10)));
            setRolloverEnabled(true);
            setFocusPainted(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover() || hasFocus()) {
                copy.setColor(NewsStyle.blend(NewsStyle.accent(), NewsStyle.card(), .09f));
                copy.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, NewsStyle.scale(7), NewsStyle.scale(7));
            }
            if (hasFocus()) {
                copy.setColor(NewsStyle.accent());
                copy.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, NewsStyle.scale(7), NewsStyle.scale(7));
            }
            copy.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class Thumbnail extends JButton {
        private final NewsItem item;
        private final Consumer<Disposable> register;
        private BufferedImage image;
        private boolean failed;

        Thumbnail(NewsItem item, Consumer<Disposable> register) {
            this.item = item;
            this.register = register;
            setPreferredSize(new Dimension(NewsStyle.scale(152), NewsStyle.scale(66)));
            setMinimumSize(getPreferredSize());
            setFont(NewsStyle.font(11, false));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
            setFocusPainted(false);
            getAccessibleContext().setAccessibleName(item.title);
            addActionListener(event -> {
                if (failed) {
                    load();
                }
            });
            load();
        }

        private void load() {
            failed = false;
            setEnabled(false);
            setText(GetText.tr("Loading image..."));
            register.accept(NewsImageManager.getThumbnail(item.imageUrl).observeOn(SwingSchedulers.edt()).subscribe(
                value -> {
                    image = value;
                    setText("");
                    setToolTipText(item.title);
                    repaint();
                }, error -> {
                    failed = true;
                    setEnabled(true);
                    setText(GetText.tr("Retry image"));
                    setToolTipText(GetText.tr("Image unavailable. Click to retry."));
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    repaint();
                }));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copy.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), NewsStyle.scale(7), NewsStyle.scale(7)));
            if (image == null) {
                copy.setColor(NewsStyle.border());
                copy.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(copy);
            } else {
                copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double ratio = Math.max(getWidth() / (double) image.getWidth(), getHeight() / (double) image.getHeight());
                int width = (int) Math.ceil(image.getWidth() * ratio);
                int height = (int) Math.ceil(image.getHeight() * ratio);
                copy.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
            }
            copy.dispose();
        }
    }
}
