/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.evnt.ConsoleCloseEvent;
import com.atlauncher.evnt.ConsoleOpenEvent;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.gui.components.Console;
import com.atlauncher.utils.Utils;

public class LauncherConsole extends JFrame {
    private JScrollPane scrollPane;
    public Console console;
    private HTMLEditorKit kit;
    private HTMLDocument doc;
    private ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    public LauncherConsole() {
        setTitle("ATLauncher Console " + Constants.VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setMinimumSize(new Dimension(600, 400));
        this.setLayout(new BorderLayout());

        console = new Console();
        console.setFont(App.THEME.getConsoleFont().deriveFont(Utils.getBaseFontSize()));
        console.setForeground(App.THEME.getConsoleTextColor());
        console.setSelectionColor(App.THEME.getSelectionColor());

        setupContextMenu(); // Setup the right click menu

        bottomBar = new ConsoleBottomBar();

        scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    @Override
    public void setVisible(boolean flag){
        super.setVisible(flag);
        if(flag){
            ConsoleOpenManager.post(new ConsoleOpenEvent());
        } else{
            ConsoleCloseManager.post(new ConsoleCloseEvent());
        }
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem("Copy");
        copy.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringSelection text = new StringSelection(console.getSelectedText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(text, null);
            }
        });
        contextMenu.add(copy);

        console.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (console.getSelectedText() != null) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        contextMenu.show(console, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    @Deprecated
    public void log(String text) {
        log(text, LogMessageType.info, false);
    }

    @Deprecated
    public void log(String text, boolean error) {
        log(text, LogMessageType.error, false);
    }

    public void log(String text, LogMessageType type, boolean isMinecraft) {
        /*
        synchronized (kit) {
            text = text.replace(App.settings.getBaseDir().getAbsolutePath(), "**USERSDIR**");
            try {
                kit.insertHTML(
                        doc,
                        doc.getLength(),
                        HTMLifier.wrap(String.format("[%s] ", Timestamper.now())).bold()
                                .font(type.getColourCode())
                                + text + (doc.getLength() == 0 ? "" : "<br/>"), 0, 0, null);
                if (!isMinecraft) {
                    FileWriter fw = null;
                    PrintWriter pw = null;
                    try {
                        fw = new FileWriter(LOG_FILE, true);
                        pw = new PrintWriter(fw);
                        pw.println(String.format("[%s] %s", Timestamper.now(), text));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (fw != null) {
                                fw.close();
                            }
                            if (pw != null) {
                                pw.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            console.setCaretPosition(console.getDocument().getLength());
        } */
    }

    /**
     * Logs a stack trace to the console window
     *
     * @param e The text to show in the console
     */
    @Deprecated
    public void logStackTrace(Exception e) {
        e.printStackTrace();
        log(e.getMessage(), LogMessageType.error, false);
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString() != null) {
                log(element.toString(), LogMessageType.error, false);
            }
        }
    }

    /**
     * Logs text to the console window from Minecraft
     *
     * @param text The text to show in the console
     */
    public void logMinecraft(String text) {
        String message = null; // The log message
        LogMessageType type = null; // The log message type
        if (text.contains("[INFO] [STDERR]")) {
            message = text.substring(text.indexOf("[INFO] [STDERR]"));
            type = LogMessageType.warning;
        } else if (text.contains("[INFO]")) {
            message = text.substring(text.indexOf("[INFO]"));
            if (message.contains("CONFLICT")) {
                type = LogMessageType.error;
            } else if (message.contains("overwriting existing item")) {
                type = LogMessageType.warning;
            } else {
                type = LogMessageType.info;
            }
        } else if (text.contains("[WARNING]")) {
            message = text.substring(text.indexOf("[WARNING]"));
            type = LogMessageType.warning;
        } else if (text.contains("WARNING:")) {
            message = text.substring(text.indexOf("WARNING:"));
            type = LogMessageType.warning;
        } else if (text.contains("INFO:")) {
            message = text.substring(text.indexOf("INFO:"));
            type = LogMessageType.info;
        } else if (text.contains("Exception")) {
            message = text;
            type = LogMessageType.error;
        } else if (text.contains("[SEVERE]")) {
            message = text.substring(text.indexOf("[SEVERE]"));
            type = LogMessageType.error;
        } else if (text.contains("[Sound Library Loader/ERROR]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/ERROR]"));
            type = LogMessageType.error;
        } else if (text.contains("[Sound Library Loader/WARN]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/WARN]"));
            type = LogMessageType.warning;
        } else if (text.contains("[Sound Library Loader/INFO]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/INFO]"));
            type = LogMessageType.info;
        } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/ERROR]"));
            type = LogMessageType.error;
        } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]"));
            type = LogMessageType.warning;
        } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]"));
            type = LogMessageType.info;
        } else if (text.contains("[Client thread/ERROR]")) {
            message = text.substring(text.indexOf("[Client thread/ERROR]"));
            type = LogMessageType.error;
        } else if (text.contains("[Client thread/WARN]")) {
            message = text.substring(text.indexOf("[Client thread/WARN]"));
            type = LogMessageType.warning;
        } else if (text.contains("[Client thread/INFO]")) {
            message = text.substring(text.indexOf("[Client thread/INFO]"));
            type = LogMessageType.info;
        } else if (text.contains("[Server thread/ERROR]")) {
            message = text.substring(text.indexOf("[Server thread/ERROR]"));
            type = LogMessageType.error;
        } else if (text.contains("[Server thread/WARN]")) {
            message = text.substring(text.indexOf("[Server thread/WARN]"));
            type = LogMessageType.warning;
        } else if (text.contains("[Server thread/INFO]")) {
            message = text.substring(text.indexOf("[Server thread/INFO]"));
            type = LogMessageType.info;
        } else if (text.contains("[main/ERROR]")) {
            message = text.substring(text.indexOf("[main/ERROR]"));
            type = LogMessageType.error;
        } else if (text.contains("[main/WARN]")) {
            message = text.substring(text.indexOf("[main/WARN]"));
            type = LogMessageType.warning;
        } else if (text.contains("[main/INFO]")) {
            message = text.substring(text.indexOf("[main/INFO]"));
            type = LogMessageType.info;
        } else {
            message = text;
            type = LogMessageType.info;
        }
        log(message, type, true);
    }

    /**
     * Returns a string with the text currently in the console
     *
     * @return String Console Text
     */
    public String getLog() {
        Html2Text parser = new Html2Text();
        StringReader in = new StringReader(console.getText());
        try {
            parser.parse(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
        }
        return parser.getText();
    }

    public void showKillMinecraft() {
        bottomBar.showKillMinecraft();
    }

    public void hideKillMinecraft() {
        bottomBar.hideKillMinecraft();
    }

    public void setupLanguage() {
        copy.setText(App.settings.getLocalizedString("common.copy"));
        bottomBar.setupLanguage();
    }

    public void clearConsole() {
        console.setText(null);
    }

}