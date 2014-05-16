/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.io.IOException;
import java.io.StringReader;

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
import com.atlauncher.utils.HTMLifier;
import com.atlauncher.utils.Timestamper;
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

    public void log(String text) {
        synchronized (kit) {
            try {
                if (doc.getLength() == 0) {
                    text = text.replace("<br/>", ""); // Remove line break on first log entry
                }
                kit.insertHTML(doc, doc.getLength(), text, 0, 0, null);
                console.setCaretPosition(console.getDocument().getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
            message = text.substring(text.indexOf("[INFO] [STDERR]") + 15);
            type = LogMessageType.warning;
        } else if (text.contains("[INFO]")) {
            message = text.substring(text.indexOf("[INFO]") + 6);
            if (message.contains("CONFLICT")) {
                type = LogMessageType.error;
            } else if (message.contains("overwriting existing item")) {
                type = LogMessageType.warning;
            } else {
                type = LogMessageType.info;
            }
        } else if (text.contains("[WARNING]")) {
            message = text.substring(text.indexOf("[WARNING]") + 9);
            type = LogMessageType.warning;
        } else if (text.contains("WARNING:")) {
            message = text.substring(text.indexOf("WARNING:") + 8);
            type = LogMessageType.warning;
        } else if (text.contains("INFO:")) {
            message = text.substring(text.indexOf("INFO:") + 5);
            type = LogMessageType.info;
        } else if (text.contains("Exception")) {
            message = text;
            type = LogMessageType.error;
        } else if (text.contains("[SEVERE]")) {
            message = text.substring(text.indexOf("[SEVERE]") + 8);
            type = LogMessageType.error;
        } else if (text.contains("[Sound Library Loader/ERROR]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/ERROR]") + 28);
            type = LogMessageType.error;
        } else if (text.contains("[Sound Library Loader/WARN]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/WARN]") + 27);
            type = LogMessageType.warning;
        } else if (text.contains("[Sound Library Loader/INFO]")) {
            message = text.substring(text.indexOf("[Sound Library Loader/INFO]") + 27);
            type = LogMessageType.info;
        } else if (text.contains("[MCO Availability Checker #1/ERROR]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/ERROR]") + 35);
            type = LogMessageType.error;
        } else if (text.contains("[MCO Availability Checker #1/WARN]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/WARN]") + 34);
            type = LogMessageType.warning;
        } else if (text.contains("[MCO Availability Checker #1/INFO]")) {
            message = text.substring(text.indexOf("[MCO Availability Checker #1/INFO]") + 34);
            type = LogMessageType.info;
        } else if (text.contains("[Client thread/ERROR]")) {
            message = text.substring(text.indexOf("[Client thread/ERROR]") + 21);
            type = LogMessageType.error;
        } else if (text.contains("[Client thread/WARN]")) {
            message = text.substring(text.indexOf("[Client thread/WARN]") + 20);
            type = LogMessageType.warning;
        } else if (text.contains("[Client thread/INFO]")) {
            message = text.substring(text.indexOf("[Client thread/INFO]") + 20);
            type = LogMessageType.info;
        } else if (text.contains("[Server thread/ERROR]")) {
            message = text.substring(text.indexOf("[Server thread/ERROR]") + 21);
            type = LogMessageType.error;
        } else if (text.contains("[Server thread/WARN]")) {
            message = text.substring(text.indexOf("[Server thread/WARN]") + 20);
            type = LogMessageType.warning;
        } else if (text.contains("[Server thread/INFO]")) {
            message = text.substring(text.indexOf("[Server thread/INFO]") + 20);
            type = LogMessageType.info;
        } else if (text.contains("[main/ERROR]")) {
            message = text.substring(text.indexOf("[main/ERROR]") + 12);
            type = LogMessageType.error;
        } else if (text.contains("[main/WARN]")) {
            message = text.substring(text.indexOf("[main/WARN]") + 11);
            type = LogMessageType.warning;
        } else if (text.contains("[main/INFO]")) {
            message = text.substring(text.indexOf("[main/INFO]") + 11);
            type = LogMessageType.info;
        } else {
            message = text;
            type = LogMessageType.info;
        }
        String logMessage = String.format(
                "%s %s<br/>",
                HTMLifier.wrap("[" + Timestamper.now() + "] [" + type.getType() + "]").bold()
                        .font(type.getColourCode()), message);
        log(logMessage);
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