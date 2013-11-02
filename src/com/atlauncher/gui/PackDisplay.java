/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.atlauncher.App;
import com.atlauncher.data.Pack;
import com.atlauncher.data.SemiPublicPack;
import com.atlauncher.utils.Utils;

/**
 * Class for displaying packs in the Pack Tab
 * 
 * @author Ryan
 * 
 */
public class PackDisplay extends CollapsiblePanel {

    private JPanel leftPanel; // Left panel with image
    private JPanel rightPanel; // Right panel with description and actions
    private JSplitPane splitPane; // The split pane
    private JLabel packImage; // The image for the pack
    private JTextArea packDescription; // Description of the pack
    private JSplitPane packActions; // All the actions that can be performed on the pack
    private JPanel packActionsTop; // All the actions that can be performed on the pack
    private JPanel packActionsBottom; // All the actions that can be performed on the pack
    private JButton newInstance; // New Instance button
    private JButton createServer; // Create Server button
    private JButton removePack; // Remove Pack button
    private JButton support; // Support button
    private JButton website; // Website button

    public PackDisplay(final Pack pack) {
        super(pack);
        JPanel panel = super.getContentPane();
        panel.setLayout(new BorderLayout());

        leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());

        rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        splitPane = new JSplitPane();
        splitPane.setLeftComponent(leftPanel);
        splitPane.setRightComponent(rightPanel);
        splitPane.setEnabled(false);

        packImage = new JLabel(pack.getImage());

        packDescription = new JTextArea();
        packDescription.setBorder(BorderFactory.createEmptyBorder());
        packDescription.setEditable(false);
        packDescription.setHighlighter(null);
        packDescription.setLineWrap(true);
        packDescription.setWrapStyleWord(true);
        packDescription.setText(pack.getDescription());

        packActions = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        packActions.setEnabled(false);
        packActions.setDividerSize(0);

        packActionsTop = new JPanel();
        packActionsTop.setLayout(new FlowLayout());
        packActionsBottom = new JPanel();
        packActionsBottom.setLayout(new FlowLayout());
        packActions.setLeftComponent(packActionsTop);
        packActions.setRightComponent(packActionsBottom);

        newInstance = new JButton(App.settings.getLocalizedString("common.newinstance"));
        newInstance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("pack.offlinenewinstance"),
                            App.settings.getLocalizedString("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("instance.cannotcreate"),
                                App.settings.getLocalizedString("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack);
                    }
                }
            }
        });

        createServer = new JButton(App.settings.getLocalizedString("common.createserver"));
        createServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("pack.offlinecreateserver"),
                            App.settings.getLocalizedString("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                App.settings.getLocalizedString("instance.cannotcreate"),
                                App.settings.getLocalizedString("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack, true);
                    }
                }
            }
        });

        support = new JButton(App.settings.getLocalizedString("common.support"));
        support.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getSupportURL());
            }
        });

        website = new JButton(App.settings.getLocalizedString("common.website"));
        website.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getWebsiteURL());
            }
        });

        if (!pack.canCreateServer()) {
            createServer.setVisible(false);
        }

        packActionsTop.add(newInstance);
        packActionsTop.add(createServer);
        if (pack instanceof SemiPublicPack && !pack.isTester()) {
            removePack = new JButton("Remove Pack");
            removePack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    App.settings.removePack(((SemiPublicPack) pack).getCode());
                    App.settings.reloadPacksPanel();
                }
            });
            packActionsTop.add(removePack);
        }
        packActionsBottom.add(support);
        packActionsBottom.add(website);

        leftPanel.add(packImage, BorderLayout.CENTER);
        rightPanel.add(packDescription, BorderLayout.CENTER);
        rightPanel.add(packActions, BorderLayout.SOUTH);

        panel.add(splitPane, BorderLayout.CENTER);
    }
}
