/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.utils.Utils;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

/**
 * Class for displaying packs in the Pack Tab
 *
 * @author Ryan
 */
public class PackCard extends CollapsiblePanel implements RelocalizationListener {
    private static final long serialVersionUID = -2617283435728223314L;
    private final JTextArea descArea = new JTextArea();
    private final JButton newInstanceButton = new JButton(Language.INSTANCE.localize("common.newinstance"));
    private final JButton createServerButton = new JButton(Language.INSTANCE.localize("common.createserver"));
    private final JButton supportButton = new JButton(Language.INSTANCE.localize("common.support"));
    private final JButton websiteButton = new JButton(Language.INSTANCE.localize("common.website"));
    private final JPanel actionsPanel = new JPanel(new BorderLayout());
    private final JSplitPane splitter = new JSplitPane();
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final Pack pack;

    public PackCard(Pack pack) {
        super(pack);
        RelocalizationManager.addListener(this);
        this.pack = pack;

        this.splitter.setLeftComponent(new ImagePanel(pack.getImage().getImage()));
        this.splitter.setRightComponent(this.actionsPanel);
        this.splitter.setEnabled(false);

        JPanel abPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        abPanel.add(this.newInstanceButton);
        abPanel.add(this.createServerButton);
        abPanel.add(this.supportButton);
        abPanel.add(this.websiteButton);

        this.descArea.setText(pack.getDescription());
        this.descArea.setLineWrap(true);
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setWrapStyleWord(true);

        this.actionsPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.actionsPanel.add(abPanel, BorderLayout.SOUTH);
        this.actionsPanel.setPreferredSize(new Dimension(this.actionsPanel.getPreferredSize().width, 180));

        this.getContentPane().add(this.splitter);

        this.addActionListeners();
    }

    private void addActionListeners() {
        this.newInstanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("pack" +
                            ".offlinenewinstance"), Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" +
                                ".cannotcreate"), Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack);
                    }
                }
            }
        });
        this.createServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("pack" +
                            ".offlinecreateserver"), Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" +
                                ".cannotcreate"), Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack, true);
                    }
                }
            }
        });
        this.supportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getSupportURL());
            }
        });
        this.websiteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getWebsiteURL());
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.newInstanceButton.setText(Language.INSTANCE.localize("common.newinstance"));
        this.createServerButton.setText(Language.INSTANCE.localize("common.createserver"));
        this.supportButton.setText(Language.INSTANCE.localize("common.support"));
        this.websiteButton.setText(Language.INSTANCE.localize("common.website"));
    }
}