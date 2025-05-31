/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.dialogs.editinstancedialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.data.ModManagement;
import com.atlauncher.utils.OS;
import com.formdev.flatlaf.icons.FlatSearchIcon;

public class NotesSection extends SectionPanel {
    private final JTextField searchField = new JTextField();
    private final JTextArea noteTextArea = new JTextArea();
    private final Highlighter highlighter = new DefaultHighlighter();
    public final JCheckBox wrapCheckBox = new JCheckBox(GetText.tr("Wrap Text"));
    private int lastSearchIndex = 0;
    private final JScrollPane noteScrollPane = new JScrollPane();
    private Timer saveTimer = null;
    private final String saveLabelText = GetText.tr("Saved when window closed or press '{0} + S'",
        OS.isMac() ? "Cmd" : "Ctrl");
    private final JLabel saveLabel = new JLabel(saveLabelText);

    public NotesSection(EditDialog parent, ModManagement serverOrInstance) {
        super(parent, serverOrInstance);

        setupComponents();
    }

    private void executeSearch() {
        highlighter.removeAllHighlights();

        String searchTerm = searchField.getText().toLowerCase();
        String text = noteTextArea.getText().toLowerCase();
        int index = text.indexOf(searchTerm, lastSearchIndex + 1);

        // loop back to start if nothing found and not at the start
        if (index == -1 && lastSearchIndex != 0) {
            index = text.indexOf(searchTerm, 0);
        }

        if (index >= 0) {
            lastSearchIndex = index;

            try {
                highlighter.addHighlight(index, lastSearchIndex + searchTerm.length(),
                    DefaultHighlighter.DefaultPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupComponents() {
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 5));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        searchField.setPreferredSize(new Dimension(250, 23));
        searchField.setMinimumSize(new Dimension(250, 23));
        searchField.setMaximumSize(new Dimension(250, 23));
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    executeSearch();
                }
            }
        });
        searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSearchIcon());
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.clearCallback", (Runnable) () -> {
            searchField.setText("");
            lastSearchIndex = 0;
            executeSearch();
        });

        wrapCheckBox.setSelected(instanceOrServer.shouldWrapNotes());
        wrapCheckBox.addActionListener(e -> {
            noteTextArea.setLineWrap(wrapCheckBox.isSelected());
            instanceOrServer.setShouldWrapNotes(wrapCheckBox.isSelected());
        });

        saveLabel.setFont(saveLabel.getFont().deriveFont(12f));

        topPanel.add(saveLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(wrapCheckBox);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(searchField);

        add(topPanel, BorderLayout.NORTH);

        noteTextArea.setText(instanceOrServer.getNotes());
        noteTextArea.setLineWrap(instanceOrServer.shouldWrapNotes());
        noteTextArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                boolean isCtrlPressed = OS.isMac() ? e.isMetaDown() : e.isControlDown();

                if (key == KeyEvent.VK_S && isCtrlPressed) {
                    saveNotes();
                }
            }
        });

        noteScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        noteScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        noteScrollPane.setViewportView(noteTextArea);

        add(noteScrollPane, BorderLayout.CENTER);
    }

    public void saveNotes() {
        instanceOrServer.setNotes(noteTextArea.getText());
        instanceOrServer.save();

        saveLabel.setText(GetText.tr("Saved"));

        if (saveTimer != null && saveTimer.isRunning()) {
            saveTimer.stop();
        }

        saveTimer = new Timer(5000, e -> {
            SwingUtilities.invokeLater(() -> {
                saveLabel.setText(saveLabelText);
            });
        });
        saveTimer.setRepeats(false);
        saveTimer.start();

    }

    public String getNotes() {
        return noteTextArea.getText();
    }

    @Override
    public void updateUIState() {
    }
}
