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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.NewsFeedState;
import com.atlauncher.data.NewsItem;
import com.atlauncher.data.NewsSource;
import com.atlauncher.evnt.listener.ThemeListener;
import com.atlauncher.evnt.manager.ThemeManager;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.panels.LoadingPanel;
import com.atlauncher.gui.tabs.Tab;
import com.atlauncher.viewmodel.base.INewsViewModel;
import com.atlauncher.viewmodel.impl.NewsViewModel;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/** The launcher's two independently paginated news feeds. */
public class NewsTab extends HierarchyPanel implements Tab, ThemeListener {
    private final EnumMap<NewsSource, SourcePanel> sources = new EnumMap<>(NewsSource.class);
    private final EnumMap<NewsSource, Point> positions = new EnumMap<>(NewsSource.class);
    private NewsSource selected = NewsSource.LAUNCHER;
    private INewsViewModel viewModel;
    private CompositeDisposable viewSubscriptions;
    private JPanel sourceDeck;

    public NewsTab() {
        super(new BorderLayout());
    }

    @Override
    protected void createViewModel() {
        viewModel = new NewsViewModel();
    }

    @Override
    protected void onShow() {
        viewSubscriptions = new CompositeDisposable();
        addDisposable(viewSubscriptions);
        ThemeManager.addListener(this);
        buildView();
    }

    private void buildView() {
        JPanel controls = NewsStyle.panel(new FlowLayout(FlowLayout.LEFT, NewsStyle.scale(8), 0));
        // Keep this gap outside the viewport so scrolling cards never touch the buttons.
        controls.setBorder(BorderFactory.createEmptyBorder(NewsStyle.scale(16), NewsStyle.scale(20),
            NewsStyle.scale(12), NewsStyle.scale(20)));
        ButtonGroup group = new ButtonGroup();
        sourceDeck = NewsStyle.panel(new CardLayout());
        for (NewsSource source : NewsSource.values()) {
            SourceButton button = new SourceButton(source == NewsSource.LAUNCHER
                ? GetText.tr("ATLauncher news") : GetText.tr("Minecraft news"));
            button.setName(source == NewsSource.LAUNCHER ? "atlauncherNewsSource" : "minecraftNewsSource");
            button.setSelected(source == selected);
            button.addActionListener(event -> selectSource(source));
            group.add(button);
            controls.add(button);
            SourcePanel panel = new SourcePanel(source);
            sources.put(source, panel);
            sourceDeck.add(panel, source.name());
            viewSubscriptions.add(viewModel.getNews(source).subscribe(panel::render));
        }
        add(controls, BorderLayout.NORTH);
        add(sourceDeck, BorderLayout.CENTER);
        selectSource(selected);
        revalidate();
        repaint();
    }

    private void selectSource(NewsSource source) {
        selected = source;
        ((CardLayout) sourceDeck.getLayout()).show(sourceDeck, source.name());
        viewModel.loadNews(source);
        invokeLater(() -> sources.get(source).maybeLoadMore());
    }

    private void savePositions() {
        sources.forEach((source, panel) -> {
            if (panel.scroll != null) {
                positions.put(source, panel.scroll.getViewport().getViewPosition());
            }
        });
    }

    @Override
    protected void onDestroy() {
        savePositions();
        ThemeManager.removeListener(this);
        viewSubscriptions.clear();
        sources.clear();
        removeAll();
    }

    @Override
    public void onThemeChange() {
        savePositions();
        viewSubscriptions.clear();
        sources.clear();
        removeAll();
        buildView();
    }

    @Override
    public String getTitle() {
        return GetText.tr("News");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "News";
    }

    private final class SourcePanel extends JPanel {
        private final NewsSource source;
        private final List<NewsItem> renderedItems = new ArrayList<>();
        private final CompositeDisposable imageSubscriptions = new CompositeDisposable();
        private NewsFeedState state;
        private JScrollPane scroll;
        private NewsFeed feed;
        private JPanel footer;
        private boolean updating;
        private int renderRevision;

        SourcePanel(NewsSource source) {
            super(new BorderLayout());
            this.source = source;
            setOpaque(false);
            viewSubscriptions.add(imageSubscriptions);
        }

        void render(NewsFeedState next) {
            if (sources.get(source) != this) {
                return;
            }
            state = next;
            if (!next.initialized) {
                removeAll();
                if (next.error != null) {
                    add(message(GetText.tr("Unable to load news."), true), BorderLayout.CENTER);
                } else {
                    add(new LoadingPanel(GetText.tr("Loading news...")), BorderLayout.CENTER);
                }
                revalidate();
                repaint();
                return;
            }
            Point position = scroll == null ? positions.getOrDefault(source, new Point())
                : scroll.getViewport().getViewPosition();
            if (scroll == null) {
                removeAll();
                feed = new NewsFeed();
                scroll = new JScrollPane(feed, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                scroll.setName(source == NewsSource.LAUNCHER ? "atlauncherNewsScroll" : "minecraftNewsScroll");
                scroll.setBorder(BorderFactory.createEmptyBorder());
                scroll.getViewport().setBackground(NewsStyle.background());
                scroll.getVerticalScrollBar().setUnitIncrement(NewsStyle.scale(24));
                scroll.getVerticalScrollBar().addAdjustmentListener(event -> maybeLoadMore());
                add(scroll, BorderLayout.CENTER);
            }
            updating = true;
            boolean append = renderedItems.size() <= next.items.size();
            for (int index = 0; append && index < renderedItems.size(); index++) {
                append = renderedItems.get(index) == next.items.get(index);
            }
            if (!append) {
                imageSubscriptions.clear();
                feed.removeAll();
                renderedItems.clear();
                footer = null;
            }
            if (footer != null) {
                feed.remove(footer);
            }
            for (int index = renderedItems.size(); index < next.items.size(); index++) {
                NewsItem item = next.items.get(index);
                NewsStyle.add(feed, new NewsArticleCard(item, imageSubscriptions::add));
                NewsStyle.gap(feed, 12);
                renderedItems.add(item);
            }
            footer = createFooter(next);
            NewsStyle.add(feed, footer);
            revalidate();
            repaint();
            Point restore = position;
            int revision = ++renderRevision;
            invokeLater(() -> {
                if (sources.get(source) != this || revision != renderRevision) {
                    return;
                }
                int maximum = Math.max(0, feed.getHeight() - scroll.getViewport().getExtentSize().height);
                scroll.getViewport().setViewPosition(new Point(0, Math.min(restore.y, maximum)));
                updating = false;
                maybeLoadMore();
            });
        }

        private JPanel createFooter(NewsFeedState next) {
            if (next.error != null) {
                return message(GetText.tr("Unable to load more news."), true);
            }
            if (next.loading) {
                JPanel loading = NewsStyle.panel(new BorderLayout());
                loading.add(new LoadingPanel(GetText.tr("Loading news...")));
                loading.setPreferredSize(new Dimension(1, NewsStyle.scale(64)));
                loading.setMaximumSize(new Dimension(Integer.MAX_VALUE, NewsStyle.scale(64)));
                return loading;
            }
            JPanel footer = NewsStyle.panel(new FlowLayout(FlowLayout.CENTER));
            if (next.items.isEmpty()) {
                footer.add(NewsStyle.label(GetText.tr("No news yet."), 12, false, NewsStyle.muted()));
            } else if (!next.hasMore) {
                footer.add(NewsStyle.label(GetText.tr("You're all caught up."), 12, false, NewsStyle.muted()));
            }
            footer.setMaximumSize(new Dimension(Integer.MAX_VALUE, NewsStyle.scale(40)));
            return footer;
        }

        private JPanel message(String text, boolean retry) {
            JPanel wrapper = NewsStyle.panel(new GridBagLayout());
            JPanel content = NewsStyle.panel(new FlowLayout(FlowLayout.CENTER, NewsStyle.scale(12), NewsStyle.scale(16)));
            content.add(NewsStyle.label(text, 13, false, NewsStyle.muted()));
            if (retry) {
                JButton button = new JButton(GetText.tr("Retry"));
                button.addActionListener(event -> viewModel.retry(source));
                content.add(button);
            }
            wrapper.add(content);
            wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, NewsStyle.scale(80)));
            return wrapper;
        }

        void maybeLoadMore() {
            if (updating || source != selected || scroll == null || !scroll.isShowing() || state == null
                || !state.initialized || state.loading || !state.hasMore || state.error != null) {
                return;
            }
            int bottom = scroll.getVerticalScrollBar().getValue() + scroll.getVerticalScrollBar().getVisibleAmount();
            if (bottom >= scroll.getVerticalScrollBar().getMaximum() - NewsStyle.scale(220)) {
                viewModel.loadMore(source);
            }
        }
    }

    private static final class NewsFeed extends JPanel implements Scrollable {
        NewsFeed() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(NewsStyle.scale(4), NewsStyle.scale(20), NewsStyle.scale(20),
                NewsStyle.scale(20)));
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) {
            return NewsStyle.scale(24);
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(NewsStyle.scale(24), visible.height - NewsStyle.scale(48));
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static final class SourceButton extends JToggleButton {
        SourceButton(String text) {
            super(text);
            setFont(NewsStyle.font(13, true));
            setContentAreaFilled(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setFocusPainted(false);
            setBorder(BorderFactory.createEmptyBorder(NewsStyle.scale(10), NewsStyle.scale(15), NewsStyle.scale(10),
                NewsStyle.scale(15)));
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (isSelected() || getModel().isRollover()) {
                copy.setColor(isSelected() ? NewsStyle.blend(NewsStyle.accent(), NewsStyle.background(), .11f)
                    : NewsStyle.card());
                copy.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, NewsStyle.scale(8), NewsStyle.scale(8));
            }
            if (isSelected() || hasFocus()) {
                copy.setColor(isSelected() ? NewsStyle.blend(NewsStyle.accent(), NewsStyle.background(), .65f)
                    : NewsStyle.accent());
                copy.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, NewsStyle.scale(8), NewsStyle.scale(8));
            }
            copy.dispose();
            setForeground(isSelected() ? NewsStyle.accent() : NewsStyle.muted());
            super.paintComponent(graphics);
        }
    }
}
