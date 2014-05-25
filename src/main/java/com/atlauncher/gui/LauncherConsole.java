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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.atlauncher.App;
import com.atlauncher.data.Constants;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.utils.Utils;

public class LauncherConsole extends JFrame {

    // Size of initial window
    private final Dimension WINDOW_SIZE = new Dimension(600, 400);
    // Minimum size the window can be
    private final Dimension MINIMUM_SIZE = new Dimension(600, 400);
    private final BorderLayout LAYOUT_MANAGER = new BorderLayout();

    private JScrollPane scrollPane;
    private JEditorPane console;
    private HTMLEditorKit kit;
    private HTMLDocument doc;
    private ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    public LauncherConsole() {
        setSize(WINDOW_SIZE);
        setTitle("ATLauncher Console " + Constants.VERSION);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Utils.getImage("/assets/image/Icon.png"));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(LAYOUT_MANAGER);

        console = new JEditorPane("text/html", "") {
            public boolean getScrollableTracksViewportWidth() {
                return true; // Fixes issues with resizing from big to small and text not shrinking
            }
        };
        console.setFont(App.THEME.getConsoleFont());
        console.setForeground(App.THEME.getConsoleTextColour());
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        console.setEditable(false);
        console.setSelectionColor(App.THEME.getSelectionColour());
        console.setEditorKit(kit);
        console.setDocument(doc);

        setupContextMenu(); // Setup the right click menu

        bottomBar = new ConsoleBottomBar();

        scrollPane = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // Make sure the size doesn't go below the minimum size
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension d = LauncherConsole.this.getSize();
                Dimension minD = LauncherConsole.this.getMinimumSize();
                if (d.width < minD.width) {
                    d.width = minD.width;
                }
                if (d.height < minD.height) {
                    d.height = minD.height;
                }
                LauncherConsole.this.setSize(d);
            }
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent arg0) {
                App.settings.setConsoleVisible(false);
            }
        });
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
        synchronized (kit) {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().substring(0, timestamp.toString().lastIndexOf("."));
            try {
                if (doc.getLength() == 0) {
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"" + type.getColourCode()
                            + "\">[" + time + "]</font></b> " + text, 0, 0, null);
                } else {
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"" + type.getColourCode()
                            + "\">[" + time + "]</font></b> " + text + "<br/>", 0, 0, null);
                }
                if (!isMinecraft) {
                    PrintWriter out = new PrintWriter(new FileWriter(new File(
                            App.settings.getBaseDir(), "ATLauncher-Log-1.txt"), true));
                    out.println("[" + time + "] " + text);
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
            console.setCaretPosition(console.getDocument().getLength());
        }
    }

    /**
     * Logs a stack trace to the console window
     * 
     * @param text
     *            The text to show in the console
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
     * @param text
     *            The text to show in the console
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