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
    private JPanel instanceActions; // All the actions that can be performed on the instance
    private JButton play; // Play button
    private JButton backup; // Backup button
    private JButton delete; // Delete button

    public InstanceDisplay(final Instance instance) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(instance.getName() + " ("
                + instance.getPackName() + " " + instance.getVersion() + ")"));

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

        instanceActions = new JPanel();
        instanceActions.setLayout(new FlowLayout());
        play = new JButton("Play");
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Account account = LauncherFrame.settings.getAccount();
                if (!account.isRemembered()) {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    JLabel passwordLabel = new JLabel("Enter password for " + account.getUsername());
                    JPasswordField passwordField = new JPasswordField();
                    panel.add(passwordLabel, BorderLayout.NORTH);
                    panel.add(passwordField, BorderLayout.CENTER);
                    int ret = JOptionPane.showConfirmDialog(LauncherFrame.settings.getParent(),
                            panel, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
                    if (ret == JOptionPane.OK_OPTION) {
                        account.setPassword(new String(passwordField.getPassword()));
                    } else {
                        return;
                    }
                }
                boolean loggedIn = false;
                String url = null;
                String sess = null;
                try {
                    url = "https://login.minecraft.net/?user="
                            + URLEncoder.encode(account.getUsername(), "UTF-8") + "&password="
                            + URLEncoder.encode(account.getPassword(), "UTF-8") + "&version=999";
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
                            "<html><center>Couldn't login to minecraft servers<br/><br/>" + auth
                                    + "</center></html>", "Error Logging In",
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    final String session = sess;
                    Object launcher = new Thread() {
                        public void run() {
                            try {
                                LauncherFrame.settings.getParent().setVisible(false);
                                Process process = MCLauncher.launch(account, instance, session);
                                InputStream is = process.getErrorStream();
                                InputStreamReader isr = new InputStreamReader(is);
                                BufferedReader br = new BufferedReader(isr);
                                String line;
                                while ((line = br.readLine()) != null) {
                                    LauncherFrame.settings.getConsole().log(line);
                                }
                                LauncherFrame.settings.getParent().setVisible(true);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    };
                    ((Thread) launcher).start();
                }
            }
        });
        backup = new JButton("Backup");
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
        instanceActions.add(play);
        instanceActions.add(backup);
        instanceActions.add(delete);

        leftPanel.add(instanceImage, BorderLayout.CENTER);
        rightPanel.add(instanceDescription, BorderLayout.CENTER);
        rightPanel.add(instanceActions, BorderLayout.SOUTH);

        add(splitPane, BorderLayout.CENTER);
    }
}
