/**
 * Copyright 2013 by ATLauncher and Contributors
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
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import com.atlauncher.App;

public class LauncherConsole extends JFrame {

    // Size of initial window
    private final Dimension WINDOW_SIZE = new Dimension(600, 400);
    // Minimum size the window can be
    private final Dimension MINIMUM_SIZE = new Dimension(520, 200);
    private final BorderLayout LAYOUT_MANAGER = new BorderLayout();
    private final Color BASE_COLOR = new Color(40, 45, 50);

    private JScrollPane scrollPane;
    private JEditorPane console;
    private HTMLEditorKit kit;
    private HTMLDocument doc;
    private ConsoleBottomBar bottomBar;
    private JPopupMenu contextMenu; // Right click menu

    private JMenuItem copy;

    public LauncherConsole() {
        setSize(WINDOW_SIZE);
        setTitle("ATLauncher Console %VERSION%");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Utils.getImage("/resources/Icon.png"));
        setMinimumSize(MINIMUM_SIZE);
        setLayout(LAYOUT_MANAGER);

        setupLookAndFeel(); // Setup the look and feel for the Console

        console = new JEditorPane("text/html", "") {
            public boolean getScrollableTracksViewportWidth() {
                return true; // Fixes issues with resizing from big to small and text not shrinking
            }
        };
        kit = new HTMLEditorKit();
        doc = new HTMLDocument();
        console.setEditable(false);
        console.setSelectionColor(Color.GRAY);
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
                setVisible(false);
            }
        });
    }

    private void setupContextMenu() {
        contextMenu = new JPopupMenu();

        copy = new JMenuItem("Copy Log");
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

    private void setupLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            App.settings.getConsole().logStackTrace(e);
        }

        UIManager.put("control", BASE_COLOR);
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", Color.BLACK);
        UIManager.put("nimbusFocus", BASE_COLOR);
        UIManager.put("nimbusBorder", BASE_COLOR);
        UIManager.put("nimbusLightBackground", BASE_COLOR);
        UIManager.put("info", BASE_COLOR);
    }

    public void log(String text) {
        log(text, false);
    }

    /**
     * Logs text to the console window and to file
     * 
     * @param text
     *            The text to show in the console and file
     */
    public void log(String text, boolean stackTrace) {
        synchronized (kit) {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().substring(0, timestamp.toString().lastIndexOf("."));
            try {
                if (stackTrace) {
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#EE2222\">[" + time
                            + "]</font></b> " + text + "<br/>", 0, 0, null);
                } else {
                    if (doc.getLength() == 0) {
                        kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#89c236\">[" + time
                                + "]</font></b> " + text, 0, 0, null);
                    } else {
                        kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#89c236\">[" + time
                                + "]</font></b> " + text + "<br/>", 0, 0, null);
                    }
                }
                PrintWriter out = new PrintWriter(new FileWriter(new File(
                        App.settings.getBaseDir(), "ATLauncher-Log-1.txt"), true));
                out.println("[" + time + "] " + text);
                out.close();
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
    public void logStackTrace(Exception e) {
        e.printStackTrace();
        log(e.getMessage(), true);
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            if (element.toString() != null) {
                log(element.toString(), true);
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
        synchronized (kit) {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().substring(0, timestamp.toString().lastIndexOf("."));
            try {
                if (text.contains("[INFO] [STDERR]")) {
                    text = text.substring(text.indexOf("[INFO] [STDERR]"));
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#FFFF4C\">[" + time
                            + "]</font></b> " + text + "<br/>", 0, 0, null);
                } else if (text.contains("[INFO]")) {
                    text = text.substring(text.indexOf("[INFO]"));
                    if (text.contains("CONFLICT")) {
                        kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#EE2222\">[" + time
                                + "]</font></b> " + text + "<br/>", 0, 0, null);
                    } else if (text.contains("overwriting existing item")) {
                        kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#FFFF4C\">[" + time
                                + "]</font></b> " + text + "<br/>", 0, 0, null);
                    } else {
                        kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#89c236\">[" + time
                                + "]</font></b> " + text + "<br/>", 0, 0, null);
                    }
                } else if (text.contains("[WARNING]")) {
                    text = text.substring(text.indexOf("[WARNING]"));
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#FFFF4C\">[" + time
                            + "]</font></b> " + text + "<br/>", 0, 0, null);
                } else if (text.contains("[SEVERE]")) {
                    text = text.substring(text.indexOf("[SEVERE]"));
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#EE2222\">[" + time
                            + "]</font></b> " + text + "<br/>", 0, 0, null);
                } else {
                    kit.insertHTML(doc, doc.getLength(), "<b><font color=\"#89c236\">[" + time
                            + "]</font></b> " + text + "<br/>", 0, 0, null);
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