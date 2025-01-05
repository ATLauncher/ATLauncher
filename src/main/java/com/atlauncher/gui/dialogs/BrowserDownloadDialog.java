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
package com.atlauncher.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.layouts.WrapLayout;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

public final class BrowserDownloadDialog extends JDialog {
    private final List<Mod> browserDownloadMods;
    public final List<Mod> modsDownloaded = new ArrayList<>();

    private final Timer timer = new Timer();

    private final JPanel topPanel = new JPanel(new BorderLayout());
    private final JPanel mainPanel = new JPanel();
    private final JPanel bottomPanel = new JPanel(new WrapLayout());

    private JScrollPane mainScrollPanel = Utils.wrapInVerticalScroller(mainPanel, 16);
    private final JButton openFolderButton = new JButton(GetText.tr("Open Folder"));
    private final JButton openAllButton = new JButton(GetText.tr("Open All In Browser"));
    private final JButton skipRemainingButton = new JButton(GetText.tr("Skip Remaining"));
    private final JButton cancelInstallButton = new JButton(GetText.tr("Cancel Install"));

    private final Path downloadPath = FileSystem.getUserDownloadsPath();

    public boolean success = false;

    public BrowserDownloadDialog(Window parent, List<Mod> browserDownloadMods) {
        super(parent, GetText.tr("Browser Download Mods"), ModalityType.DOCUMENT_MODAL);
        this.browserDownloadMods = browserDownloadMods;

        this.setPreferredSize(new Dimension(680, 600));
        this.setMinimumSize(new Dimension(680, 600));
        this.setResizable(true);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        checkForDownloadedMods(false);
        if (success) {
            return;
        }

        Analytics.sendScreenView("Browser Download Mods");

        setupComponents();

        setupFileWatching();

        this.setLocationRelativeTo(parent);
        this.setVisible(true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent arg0) {
                timer.cancel();
                timer.purge();
            }
        });
    }

    private void setupFileWatching() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkForDownloadedMods(true);
            }
        }, 5000, 5000);
    }

    protected void checkForDownloadedMods(boolean allowReload) {
        boolean reloadMainPanel = false;
        for (Mod mod : browserDownloadMods) {
            if (modsDownloaded.contains(mod)) {
                continue;
            }

            Path downloadsPath = downloadPath.resolve(mod.file);
            Path downloadsPath2 = downloadPath.resolve(mod.file.replace(" ", "+"));
            Path finalLocation = FileSystem.DOWNLOADS.resolve(mod.file);
            Path finalLocation2 = FileSystem.DOWNLOADS.resolve(mod.file.replace(" ", "+"));

            // first check the user downloads folder (if not the same as final)
            if (!downloadsPath.equals(finalLocation) && Files.exists(downloadsPath)
                    && downloadsPath.toFile().length() == mod.filesize) {
                if (mod.md5 != null
                        && Hashing.md5(downloadsPath).equals(Hashing.toHashCode(mod.md5))) {
                    FileUtils.moveFile(downloadsPath, finalLocation, true);
                }

                if (mod.sha1 != null
                        && Hashing.sha1(downloadsPath).equals(Hashing.toHashCode(mod.sha1))) {
                    FileUtils.moveFile(downloadsPath, finalLocation, true);
                }

                if (mod.sha512 != null
                        && Hashing.sha512(downloadsPath).equals(Hashing.toHashCode(mod.sha512))) {
                    FileUtils.moveFile(downloadsPath, finalLocation, true);
                }
            }

            // first check the user downloads folder with spaces replaced (if not the same
            // as final)
            if (!downloadsPath2.equals(finalLocation2) && Files.exists(downloadsPath2)
                    && downloadsPath2.toFile().length() == mod.filesize) {
                if (mod.md5 != null
                        && Hashing.md5(downloadsPath2).equals(Hashing.toHashCode(mod.md5))) {
                    FileUtils.moveFile(downloadsPath2, finalLocation, true);
                }

                if (mod.sha1 != null
                        && Hashing.sha1(downloadsPath2).equals(Hashing.toHashCode(mod.sha1))) {
                    FileUtils.moveFile(downloadsPath2, finalLocation, true);
                }

                if (mod.sha512 != null
                        && Hashing.sha512(downloadsPath2).equals(Hashing.toHashCode(mod.sha512))) {
                    FileUtils.moveFile(downloadsPath2, finalLocation, true);
                }
            }

            if (Files.exists(finalLocation2)) {
                if (mod.md5 != null
                        && Hashing.md5(finalLocation2).equals(Hashing.toHashCode(mod.md5))) {
                    FileUtils.moveFile(finalLocation2, finalLocation, true);
                }

                if (mod.sha1 != null
                        && Hashing.sha1(finalLocation2).equals(Hashing.toHashCode(mod.sha1))) {
                    FileUtils.moveFile(finalLocation2, finalLocation, true);
                }

                if (mod.sha512 != null
                        && Hashing.sha512(finalLocation2).equals(Hashing.toHashCode(mod.sha512))) {
                    FileUtils.moveFile(finalLocation2, finalLocation, true);
                }
            }

            if (Files.exists(finalLocation)) {
                if (mod.md5 != null
                        && Hashing.md5(finalLocation).equals(Hashing.toHashCode(mod.md5))) {
                    modsDownloaded.add(mod);
                    reloadMainPanel = true;
                    continue;
                }

                if (mod.sha1 != null
                        && Hashing.sha1(finalLocation).equals(Hashing.toHashCode(mod.sha1))) {
                    modsDownloaded.add(mod);
                    reloadMainPanel = true;
                    continue;
                }

                if (mod.sha512 != null
                        && Hashing.sha512(finalLocation).equals(Hashing.toHashCode(mod.sha512))) {
                    modsDownloaded.add(mod);
                    reloadMainPanel = true;
                }
            }
        }

        if (modsDownloaded.size() == browserDownloadMods.size()) {
            close();
        }

        if (allowReload && reloadMainPanel) {
            setupMainPanel();
            revalidate();
            repaint();
        }
    }

    private void close() {
        success = true;

        timer.cancel();
        timer.purge();
        dispose();
    }

    private void setupComponents() {
        this.topPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        JTextPane explanationPane = new JTextPane();
        explanationPane.setContentType("text/html");
        explanationPane.setText(new HTMLBuilder().text(
                GetText.tr(
                        "In order to continue downloading this modpack, you must manually download the following mods.<br/>Simply click the Open button to open them in your browser, or alternatively you can copy the url to your clipboard to open in another browser.<br/><br/>Please download all files to <b>{0}</b> in order for the launcher to detect them.",
                        downloadPath.toAbsolutePath().toString()))
                .center().build());
        explanationPane.setEditable(false);
        explanationPane.setBackground(null);
        explanationPane.setBorder(null);
        this.topPanel.add(explanationPane, BorderLayout.CENTER);
        this.topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.RED));

        this.mainPanel.setLayout(new GridBagLayout());
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        setupMainPanel();

        this.bottomPanel.add(openFolderButton);
        this.bottomPanel.add(openAllButton);
        this.bottomPanel.add(skipRemainingButton);
        this.bottomPanel.add(cancelInstallButton);

        openFolderButton.addActionListener(l -> OS.openFileExplorer(downloadPath));

        openAllButton.addActionListener(l -> {
            for (Mod mod : browserDownloadMods) {
                if (!modsDownloaded.contains(mod)) {
                    OS.openWebBrowser(mod.url);
                }
            }
        });

        skipRemainingButton.addActionListener(l -> {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Are You Sure?"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "Skipping the remaining downloads may cause issues launching the modpack.<br/><br/>Are you sure you want to do this?"))
                            .build())
                    .setType(DialogManager.WARNING).show();

            if (ret == 0) {
                close();
            }
        });

        cancelInstallButton.addActionListener(l -> {
            timer.cancel();
            timer.purge();
            dispose();
        });

        add(this.topPanel, BorderLayout.NORTH);
        add(this.mainScrollPanel, BorderLayout.CENTER);
        add(this.bottomPanel, BorderLayout.SOUTH);
    }

    private void setupMainPanel() {
        this.mainPanel.removeAll();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel modLabel = new JLabel("Mod");
        modLabel.setFont(App.THEME.getBoldFont());
        this.mainPanel.add(modLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel filenameLabel = new JLabel("Filename");
        filenameLabel.setFont(App.THEME.getBoldFont());
        this.mainPanel.add(filenameLabel, gbc);

        gbc.gridx++;
        gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel actionsLabel = new JLabel("Actions");
        actionsLabel.setFont(App.THEME.getBoldFont());
        this.mainPanel.add(actionsLabel, gbc);

        for (Mod mod : browserDownloadMods) {
            gbc.gridy++;
            gbc.gridx = 0;

            gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            JTextField nameTextField = new JTextField(mod.name);
            nameTextField.setEditable(false);
            nameTextField.setBorder(null);
            nameTextField.setBackground(null);
            nameTextField.setPreferredSize(new Dimension(200, 33));
            this.mainPanel.add(nameTextField, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            JTextField fileTextField = new JTextField(mod.file);
            fileTextField.setEditable(false);
            fileTextField.setBorder(null);
            fileTextField.setBackground(null);
            fileTextField.setPreferredSize(new Dimension(200, 33));
            this.mainPanel.add(fileTextField, gbc);

            gbc.gridx++;
            gbc.insets = UIConstants.LEFT_TO_RIGHT_SPACER;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;

            if (modsDownloaded.contains(mod)) {
                this.mainPanel.add(new JLabel(GetText.tr("Download Complete")), gbc);
            } else {
                JPanel buttonPanel = new JPanel(new FlowLayout());
                JButton openButton = new JButton(GetText.tr("Open"));
                buttonPanel.add(openButton, gbc);
                openButton.addActionListener(l -> OS.openWebBrowser(mod.url));

                JButton copyLinkButton = new JButton(GetText.tr("Copy Link"));
                buttonPanel.add(copyLinkButton, gbc);
                copyLinkButton.addActionListener(l -> OS.copyToClipboard(mod.url));
                this.mainPanel.add(buttonPanel, gbc);
            }
        }

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weighty = 1;
        this.mainPanel.add(new JPanel(), gbc);
    }
}
