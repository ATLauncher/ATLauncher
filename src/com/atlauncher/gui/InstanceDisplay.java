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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.mclauncher.NewMCLauncher;

/**
 * Class for displaying instances in the Instance Tab
 * 
 * @author Ryan
 * 
 */
public class InstanceDisplay extends CollapsiblePanel {

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
        super(instance);
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

        play = new JButton(App.settings.getLocalizedString("common.play"));
        play.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                final Account account = App.settings.getAccount();
                if (account == null) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.noaccount"),
                            App.settings.getLocalizedString("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    String username = account.getUsername();
                    String password = account.getPassword();
                    if (!account.isRemembered()) {
                        JPanel panel = new JPanel();
                        panel.setLayout(new BorderLayout());
                        JLabel passwordLabel = new JLabel(App.settings.getLocalizedString(
                                "instance.enterpassword", account.getMinecraftUsername()));
                        JPasswordField passwordField = new JPasswordField();
                        panel.add(passwordLabel, BorderLayout.NORTH);
                        panel.add(passwordField, BorderLayout.CENTER);
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), panel,
                                App.settings.getLocalizedString("instance.enterpasswordtitle"),
                                JOptionPane.OK_CANCEL_OPTION);
                        if (ret == JOptionPane.OK_OPTION) {
                            password = new String(passwordField.getPassword());
                        } else {
                            return;
                        }
                    }
                    boolean loggedIn = false;
                    String url = null;
                    String sess = null;
                    String auth = null;
                    if (instance.isNewLaunchMethod()) {
                        String result = newLogin(username, password);
                        JSONParser parser = new JSONParser();
                        try {
                            Object obj = parser.parse(result);
                            JSONObject jsonObject = (JSONObject) obj;
                            if (jsonObject.containsKey("accessToken")) {
                                String accessToken = (String) jsonObject.get("accessToken");
                                JSONObject profile = (JSONObject) jsonObject.get("selectedProfile");
                                String profileID = (String) profile.get("id");
                                sess = "token:" + accessToken + ":" + profileID;
                                loggedIn = true;
                            } else {
                                auth = (String) jsonObject.get("errorMessage");
                            }
                        } catch (ParseException e1) {
                            App.settings.getConsole().logStackTrace(e1);
                        }
                    } else {
                        try {
                            url = "https://login.minecraft.net/?user="
                                    + URLEncoder.encode(username, "UTF-8") + "&password="
                                    + URLEncoder.encode(password, "UTF-8") + "&version=999";
                        } catch (UnsupportedEncodingException e1) {
                            App.settings.getConsole().logStackTrace(e1);
                        }
                        auth = Utils.urlToString(url);
                        if (auth.contains(":")) {
                            String[] parts = auth.split(":");
                            if (parts.length == 5) {
                                loggedIn = true;
                                sess = parts[3];
                            }
                        }
                    }
                    if (!loggedIn) {
                        String[] options = { App.settings.getLocalizedString("common.ok") };
                        JOptionPane.showOptionDialog(
                                App.settings.getParent(),
                                "<html><center>"
                                        + App.settings.getLocalizedString(
                                                "instance.errorloggingin", "<br/><br/>" + auth)
                                        + "</center></html>", App.settings
                                        .getLocalizedString("instance.errorloggingintitle"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                                options, options[0]);
                    } else {
                        final String session = sess;
                        Thread launcher = new Thread() {
                            public void run() {
                                try {
                                    long start = System.currentTimeMillis();
                                    App.settings.getParent().setVisible(false);
                                    Process process = null;
                                    if (instance.isNewLaunchMethod()) {
                                        process = NewMCLauncher.launch(account, instance, session);
                                    } else {
                                        process = MCLauncher.launch(account, instance, session);
                                    }
                                    App.settings.showKillMinecraft(process);
                                    InputStream is = process.getInputStream();
                                    InputStreamReader isr = new InputStreamReader(is);
                                    BufferedReader br = new BufferedReader(isr);
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        App.settings.getConsole().logMinecraft(line);
                                    }
                                    App.settings.hideKillMinecraft();
                                    App.settings.getParent().setVisible(true);
                                    long end = System.currentTimeMillis();
                                    if (App.settings.enableLeaderboards()) {
                                        App.settings.apiCall(account.getMinecraftUsername(),
                                                "addleaderboardtime",
                                                (instance.getRealPack() == null ? "0" : instance
                                                        .getRealPack().getID() + ""),
                                                ((end - start) / 1000) + "");
                                    } else {
                                        App.settings.apiCall("NULL", "addleaderboardtime",
                                                (instance.getRealPack() == null ? "0" : instance
                                                        .getRealPack().getID() + ""),
                                                ((end - start) / 1000) + "");
                                    }
                                } catch (IOException e1) {
                                    App.settings.getConsole().logStackTrace(e1);
                                }
                            }
                        };
                        launcher.start();
                    }
                }
            }
        });

        // Reinstall Button

        reinstall = new JButton(App.settings.getLocalizedString("common.reinstall"));
        reinstall.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.getAccount() == null) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.cantreinstall"),
                            App.settings.getLocalizedString("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    new InstanceInstallerDialog(instance);
                }
            }
        });

        // Update Button

        update = new JButton("Update");
        update.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (App.settings.getAccount() == null) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.cantupdate"),
                            App.settings.getLocalizedString("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                } else {
                    new InstanceInstallerDialog(instance, true, false);
                }
            }
        });
        if (!instance.hasUpdate()) {
            update.setVisible(false);
        }

        // Backup Button

        backup = new JButton(App.settings.getLocalizedString("common.backup"));

        // Delete Button

        delete = new JButton(App.settings.getLocalizedString("common.delete"));
        delete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int response = JOptionPane.showConfirmDialog(App.settings.getParent(),
                        App.settings.getLocalizedString("instance.deletesure"),
                        App.settings.getLocalizedString("instance.deleteinstance"),
                        JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    App.settings.removeInstance(instance);
                }
            }
        });

        // Restore Button

        restore = new JButton(App.settings.getLocalizedString("common.restore"));

        // Check if pack can be installed and remove buttons if not

        if (!instance.canInstall()) {
            reinstall.setVisible(false);
            update.setVisible(false);
        }

        // Check is instance is playable and disable buttons if not
        if (!instance.isPlayable()) {
            for (ActionListener al : play.getActionListeners()) {
                play.removeActionListener(al);
            }
            play.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.corruptplay"),
                            App.settings.getLocalizedString("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
            for (ActionListener al : backup.getActionListeners()) {
                backup.removeActionListener(al);
            }
            backup.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.corruptbackup"),
                            App.settings.getLocalizedString("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
            for (ActionListener al : restore.getActionListeners()) {
                restore.removeActionListener(al);
            }
            restore.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(App.settings.getParent(),
                            App.settings.getLocalizedString("instance.corruptrestore"),
                            App.settings.getLocalizedString("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                }
            });
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

        panel.add(splitPane, BorderLayout.CENTER);
    }

    public String newLogin(String username, String password) {
        StringBuilder response = null;
        try {
            URL url = new URL("https://authserver.mojang.com/authenticate");
            String request = "{\"agent\":{\"name\":\"Minecraft\",\"version\":10},\"username\":\""
                    + username + "\",\"password\":\"" + password + "\",\"clientToken\":\""
                    + UUID.randomUUID() + "\"}";
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes());
            writer.flush();
            writer.close();

            // Read the result

            BufferedReader reader = null;
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            App.settings.getConsole().logStackTrace(e);
        }
        return response.toString();
    }
}
