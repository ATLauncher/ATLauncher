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
package com.atlauncher.gui.card.packbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.ftb.FTBPackArt;
import com.atlauncher.data.ftb.FTBPackArtType;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.BackgroundImageLabel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.Markdown;
import com.atlauncher.utils.OS;

public class FTBPackCard extends JPanel implements RelocalizationListener {
    private final JButton newInstanceButton = new JButton(GetText.tr("New Instance"));
    private final JButton websiteButton = new JButton(GetText.tr("Website"));

    public FTBPackCard(final FTBPackManifest pack) {
        super();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(null, pack.name, TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION,
                App.THEME.getBoldFont().deriveFont(15f)));

        RelocalizationManager.addListener(this);

        String imageUrl = null;
        if (pack.art != null) {
            Optional<FTBPackArt> art = pack.art.stream()
                    .filter(a -> a.type == FTBPackArtType.LOGO || a.type == FTBPackArtType.SQUARE)
                    .sorted(Comparator.comparingInt((FTBPackArt a) -> a.updated).reversed()).findFirst();
            if (art.isPresent()) {
                imageUrl = art.get().url;
            }
        }

        JSplitPane splitter = new JSplitPane();

        BackgroundImageLabel imageLabel = new BackgroundImageLabel(imageUrl, 150, 150);
        imageLabel.setPreferredSize(new Dimension(300, 150));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        splitter.setLeftComponent(imageLabel);

        JPanel actionsPanel = new JPanel(new BorderLayout());
        splitter.setRightComponent(actionsPanel);
        splitter.setEnabled(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        newInstanceButton.addActionListener(e -> {
            if (AccountManager.getSelectedAccount() == null) {
                DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                        .setContent(GetText.tr("Cannot create instance as you have no account selected."))
                        .setType(DialogManager.ERROR).show();

                if (AccountManager.getAccounts().isEmpty()) {
                    App.navigate(UIConstants.LAUNCHER_ACCOUNTS_TAB);
                }
            } else {
                Analytics.trackEvent(AnalyticsEvent.forPackInstall(pack));
                InstanceInstallerDialog instanceInstallerDialog = new InstanceInstallerDialog(pack);
                instanceInstallerDialog.setVisible(true);
            }
        });
        buttonsPanel.add(newInstanceButton);

        websiteButton.addActionListener(e -> OS.openWebBrowser(pack.getWebsiteUrl()));
        buttonsPanel.add(websiteButton);

        // The Feed The Beast website only displays modpacks with the 'FTB'
        // tag present, so we should hide the button for packs without the tag.
        websiteButton.setVisible(pack.hasTag("FTB"));

        JEditorPane descArea = new JEditorPane("text/html",
                String.format("<html>%s</html>", Markdown.render(pack.description)));
        descArea.setEditable(false);
        descArea.setFocusable(false);
        descArea.setHighlighter(null);
        descArea.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                OS.openWebBrowser(e.getURL());
            }
        });

        actionsPanel.add(new JScrollPane(descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        actionsPanel.add(buttonsPanel, BorderLayout.SOUTH);
        actionsPanel.setPreferredSize(new Dimension(0, 155));

        add(splitter, BorderLayout.CENTER);
    }

    @Override
    public void onRelocalization() {
        newInstanceButton.setText(GetText.tr("New Instance"));
        websiteButton.setText(GetText.tr("Website"));
    }
}
