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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.mclauncher.MCLauncher;

/**
 * Class for displaying instances in the Instance Tab
 * 
 * @author Ryan
 * 
 */
public class InstanceDisplay extends JPanel {

    private JPanel leftPanel; // Left panel with image
    private JPanel rightPanel; // Right panel with description and actions
    private JSplitPane splitPane; // The split pane
    private JLabel instanceImage; // The image for the instance
    private JTextArea instanceDescription; // Description of the instance
    private JSplitPane instanceActions; // All the actions that can be performed on the instance
    private JPanel instanceActionsTop; // All the actions that can be performed on the instance
    private JPanel instanceActionsBottom; // All the actions that can be performed on the instance
    private JButton play; // Play button
    private JButton reinstall; // Reinstall button
    private JButton update; // Update button
    private JButton backup; // Backup button
    private JButton delete; // Delete button
    private JButton restore; // Restore button

    public InstanceDisplay(final Instance instance) {
        setLayout(new BorderLayout());

        // Add titles border with name, Mac needs smaller font
        if (Utils.isMac()) {
            setBorder(new TitledBorder(null, instance.getName() + " (" + instance.getPackName()
                    + " " + instance.getVersion() + ")", TitledBorder.DEFAULT_JUSTIFICATION,
                    TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));
        } else {
            setBorder(new TitledBorder(null, instance.getName() + " (" + instance.getPackName()
                    + " " + instance.getVersion() + ")", TitledBorder.DEFAULT_JUSTIFICATION,
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

        instanceImage = new JLabel(instance.getImage());

        instanceDescription = new JTextArea();
        instanceDescription.setBorder(BorderFactory.createEmptyBorder());
        instanceDescription.setEditable(false);
        instanceDescription.setHighlighter(null);
        instanceDescription.setLineWrap(true);
        instanceDescription.setWrapStyleWord(true);
        instanceDescription.setText(instance.getPackDescription());

        instanceActions = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        instanceActions.setEnabled(false);
        instanceActions.setDividerSize(0);

        instanceActionsTop = new JPanel();
        instanceActionsTop.setLayout(new FlowLayout());
        instanceActionsBottom = new JPanel();
        instanceActionsBottom.setLayout(new FlowLayout());
        instanceActions.setLeftComponent(instanceActionsTop);
        instanceActions.setRightComponent(instanceActionsBottom);

        // Play Button

        play = new JButton("Play");
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Account account = LauncherFrame.settings.getAccount();
                if (account == null) {
                    String[] options = { "Ok" };
                    JOptionPane.showOptionDialog(LauncherFrame.settings.getParent(),
                            "Cannot play instance as you have no Account selected",
                            "No Account Selected", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    String username = account.getUsername();
                    String password = account.getPassword();
                    if (!account.isRemembered()) {
                        JPanel panel = new JPanel();
                        panel.setLayout(new BorderLayout());
                        JLabel passwordLabel = new JLabel("Enter password for "
                                + account.getUsername());
                        JPasswordField passwordField = new JPasswordField();
                        panel.add(passwordLabel, BorderLayout.NORTH);
                        panel.add(passwordField, BorderLayout.CENTER);
                        int ret = JOptionPane.showConfirmDialog(LauncherFrame.settings.getParent(),
                                panel, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
                        if (ret == JOptionPane.OK_OPTION) {
                            password = new String(passwordField.getPassword());
                        } else {
                            return;
                        }
                    }
                    boolean loggedIn = false;
                    String url = null;
                    String sess = null;
                    try {
                        url = "https://login.minecraft.net/?user="
                                + URLEncoder.encode(username, "UTF-8") + "&password="
                                + URLEncoder.encode(password, "UTF-8") + "&version=999";
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                    String auth = Utils.urlToString(url);
                    if (auth.contains(":")) {
                        String[] parts = auth.split(":");
                        if (parts.length == 5) {
                            loggedIn = true;
                            sess = parts[3];
                        }
                    }
                    if (!loggedIn) {
                        String[] options = { "Ok" };
                        JOptionPane.showOptionDialog(LauncherFrame.settings.getParent(),
                                "<html><center>Couldn't login to minecraft servers<br/><br/>"
                                        + auth + "</center></html>", "Error Logging In",
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        final String session = sess;
                        Thread launcher = new Thread() {
                            public void run() {
                                try {
                                    long start = System.currentTimeMillis();
                                    LauncherFrame.settings.getParent().setVisible(false);
                                    Process process = MCLauncher.launch(account, instance, session);
                                    InputStream is = process.getErrorStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader br = new BufferedReader(isr);
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        LauncherFrame.settings.getConsole().logMinecraft(line);
                                    }
                                    LauncherFrame.settings.getParent().setVisible(true);
                                    long end = System.currentTimeMillis();
                                    LauncherFrame.settings.getConsole().log(
                                            instance.getName() + " was played by user "
                                                    + account.getMinecraftUsername() + " for "
                                                    + ((end - start) / 1000) + " seconds");
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        };
                        LauncherFrame.settings.showKillMinecraft(launcher);
                        launcher.start();
                    }
                }
            }
        });

        // Reinstall Button

        reinstall = new JButton("Reinstall");
        reinstall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (LauncherFrame.settings.getAccount() == null) {
                    String[] options = { "Ok" };
                    JOptionPane.showOptionDialog(LauncherFrame.settings.getParent(),
                            "Cannot reinstall pack as you have no Account selected",
                            "No Account Selected", JOptionPane.DEFAULT_OPTION,
                            JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    // new ReinstallInstanceDialog(instance);
                }
            }
        });

        // Update Button

        update = new JButton("Update");
        if (!instance.hasUpdate()) {
            update.setVisible(false);
        }

        // Backup Button

        backup = new JButton("Backup");

        // Delete Button

        delete = new JButton("Delete");
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(LauncherFrame.settings.getParent(),
                        "Are you sure you want to delete this instance?", "Delete Instance",
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    LauncherFrame.settings.removeInstance(instance);
                }
            }
        });

        // Restore Button

        restore = new JButton("Restore");

        // Check if pack can be installed and remove buttons if not

        if (!instance.canInstall()) {
            reinstall.setVisible(false);
            update.setVisible(false);
        }

        // Add buttons to panels

        instanceActionsTop.add(play);
        instanceActionsTop.add(reinstall);
        instanceActionsTop.add(update);

        instanceActionsBottom.add(backup);
        instanceActionsBottom.add(delete);
        instanceActionsBottom.add(restore);

        // Add panels to other panels

        leftPanel.add(instanceImage, BorderLayout.CENTER);
        rightPanel.add(instanceDescription, BorderLayout.CENTER);
        rightPanel.add(instanceActions, BorderLayout.SOUTH);

        add(splitPane, BorderLayout.CENTER);
    }
}
