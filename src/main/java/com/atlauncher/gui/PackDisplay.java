/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.utils.Utils;

/**
 * TODO: Rewrite along with CollapsiblePanel
 *
 * Class for displaying packs in the Pack Tab
 * 
 * @author Ryan
 */
public class PackDisplay extends CollapsiblePanel implements RelocalizationListener{
    private static final long serialVersionUID = -2617283435728223314L;
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
        JScrollPane packDescriptionScoller = new JScrollPane(packDescription,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        packDescriptionScoller.getVerticalScrollBar().setUnitIncrement(16);

        packActions = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        packActions.setEnabled(false);
        packActions.setDividerSize(0);

        packActionsTop = new JPanel();
        packActionsTop.setLayout(new FlowLayout());
        packActionsBottom = new JPanel();
        packActionsBottom.setLayout(new FlowLayout());
        packActions.setLeftComponent(packActionsTop);
        packActions.setRightComponent(packActionsBottom);

        newInstance = new JButton(Language.INSTANCE.localize("common.newinstance"));
        newInstance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = { Language.INSTANCE.localize("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            Language.INSTANCE.localize("pack.offlinenewinstance"),
                            Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = { Language.INSTANCE.localize("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                Language.INSTANCE.localize("instance.cannotcreate"),
                                Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack);
                    }
                }
            }
        });

        createServer = new JButton(Language.INSTANCE.localize("common.createserver"));
        createServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.isInOfflineMode()) {
                    String[] options = { Language.INSTANCE.localize("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            Language.INSTANCE.localize("pack.offlinecreateserver"),
                            Language.INSTANCE.localize("common.offline"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    if (App.settings.getAccount() == null) {
                        String[] options = { Language.INSTANCE.localize("common.ok") };
                        JOptionPane.showOptionDialog(App.settings.getParent(),
                                Language.INSTANCE.localize("instance.cannotcreate"),
                                Language.INSTANCE.localize("instance.noaccountselected"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        new InstanceInstallerDialog(pack, true);
                    }
                }
            }
        });

        support = new JButton(Language.INSTANCE.localize("common.support"));
        support.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getSupportURL());
            }
        });

        website = new JButton(Language.INSTANCE.localize("common.website"));
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
        if (pack.isSemiPublic() && !pack.isTester()) {
            removePack = new JButton(Language.INSTANCE.localize("pack.removepack"));
            removePack.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    App.settings.removePack(pack.getCode());
                    App.settings.reloadPacksPanel();
                }
            });
            packActionsTop.add(removePack);
        }
        packActionsBottom.add(support);
        packActionsBottom.add(website);

        leftPanel.add(packImage, BorderLayout.CENTER);
        rightPanel.add(packDescriptionScoller, BorderLayout.CENTER);
        rightPanel.add(packActions, BorderLayout.SOUTH);

        panel.add(splitPane, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(rightPanel.getPreferredSize().width, 180));
    }

    @Override
    public void onRelocalization() {
        this.newInstance.setText(Language.INSTANCE.localize("common.newinstance"));
        this.support.setText(Language.INSTANCE.localize("common.support"));
        this.website.setText(Language.INSTANCE.localize("common.website"));
        this.createServer.setText(Language.INSTANCE.localize("common.createserver"));
        this.removePack.setText(Language.INSTANCE.localize("pack.removepack"));
    }
}
