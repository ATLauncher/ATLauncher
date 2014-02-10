/**
 * Copyright 2013-2014 by ATLauncher and Contributors
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.atlauncher.App;
import com.atlauncher.data.mojang.auth.AuthenticationRequest;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.Utils;

public class AuthKeySetupDialog extends JDialog {

    private JPanel top;
    private JPanel middle;
    private JPanel bottom;

    private JLabel usernameLabel;
    private JTextField username;
    private JLabel passwordLabel;
    private JPasswordField password;

    private JButton loginButton;

    public AuthKeySetupDialog() {
        super(null, "Login To Minecraft", ModalityType.APPLICATION_MODAL);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Top Panel Stuff
        top = new JPanel();
        top.add(new JLabel("Login To Minecraft"));

        // Middle Panel Stuff
        middle = new JPanel();
        middle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        usernameLabel = new JLabel("Username: ");
        middle.add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        username = new JTextField(16);
        middle.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        passwordLabel = new JLabel("Password: ");
        middle.add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        password = new JPasswordField(16);
        middle.add(password, gbc);

        // Bottom Panel Stuff
        bottom = new JPanel();
        bottom.setLayout(new FlowLayout());
        loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                AuthenticationResponse ar = null;
                try {
                    ar = Authentication.checkAccount(username.getText(),
                            new String(password.getPassword()));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                if (ar != null) {
                    if (ar.hasError()) {
                        JOptionPane.showMessageDialog(null, ar.getErrorMessage(), "Error!",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        String authKey = App.settings.apiCallReturn(ar.getSelectedProfile().getName(),
                                "checkauth", ar.getAccessToken());
                        if (authKey.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Unable to verify your account!",
                                    "Error!", JOptionPane.ERROR_MESSAGE);
                        } else {
                            App.settings.setAuthKey(authKey);
                            close();
                        }
                    }
                }
            }
        });
        bottom.add(loginButton);

        add(top, BorderLayout.NORTH);
        add(middle, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    public void close() {
        setVisible(false);
        dispose();
    }

}
