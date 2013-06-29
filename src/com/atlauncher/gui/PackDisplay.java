/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.atlauncher.data.Pack;

/**
 * Class for displaying packs in the Pack Tab
 * 
 * @author Ryan
 * 
 */
public class PackDisplay extends JPanel {

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
    private JButton support; // Support button
    private JButton website; // Website button

    public PackDisplay(final Pack pack) {
        setLayout(new BorderLayout());

        // Add titles border with name, Mac needs smaller font
        if (Utils.isMac()) {
            setBorder(new TitledBorder(null, pack.getName(), TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));
        } else {
            setBorder(new TitledBorder(null, pack.getName(), TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 15)));
        }

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
        
        newInstance = new JButton("New Instance");
        newInstance.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LauncherFrame.settings.getAccount() == null) {
                    String[] options = { "Ok" };
                    JOptionPane.showOptionDialog(LauncherFrame.settings.getParent(),
                            "Cannot create instance as you have no Account selected",
                            "No Account Selected", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new NewInstanceDialog(pack);
                }
            }
        });

        createServer = new JButton("Create Server");

        support = new JButton("Support");
        support.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getSupportURL());
            }
        });

        website = new JButton("Website");
        website.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Utils.openBrowser(pack.getWebsiteURL());
            }
        });
        
        if(!pack.canCreateServer()){
            createServer.setVisible(false);
        }

        packActionsTop.add(newInstance);
        packActionsTop.add(createServer);
        packActionsBottom.add(support);
        packActionsBottom.add(website);

        leftPanel.add(packImage, BorderLayout.CENTER);
        rightPanel.add(packDescription, BorderLayout.CENTER);
        rightPanel.add(packActions, BorderLayout.SOUTH);

        add(splitPane, BorderLayout.CENTER);
    }
}
