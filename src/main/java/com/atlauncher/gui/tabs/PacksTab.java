/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.tabs;

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.card.PackCard;
import com.atlauncher.gui.dialogs.AddPackDialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * TODO: Rewrite this for better loading
 */
public final class PacksTab
extends JPanel
implements Tab{
    private final JPanel topPanel = new JPanel();
    private final JPanel contentPanel = new JPanel();
    private final JButton addButton = new JButton(Language.INSTANCE.localize("pack.addpack"));
    private final JButton clearButton = new JButton(Language.INSTANCE.localize("common.clear"));
    private final JTextField searchField = new JTextField(16);
    private final JCheckBox serversBox = new JCheckBox(Language.INSTANCE.localize("pack.cancreateserver"));
    private final JCheckBox privateBox = new JCheckBox(Language.INSTANCE.localize("pack.privatepacksonly"));

    public PacksTab(){
        super(new BorderLayout());
        this.topPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        this.contentPanel.setLayout(new GridBagLayout());

        JScrollPane scrollPane = new JScrollPane(this.contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setBlockIncrement(16);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(this.topPanel, BorderLayout.NORTH);

        this.setupTopPanel();
        load(false);

        this.addButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new AddPackDialog();
                reload();
            }
        });
        this.searchField.addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                reload();
            }
        });
        this.privateBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                reload();
            }
        });
        this.serversBox.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e){
                reload();
            }
        });
    }

    private void setupTopPanel(){
        this.topPanel.add(this.addButton);
        this.topPanel.add(this.clearButton);
        this.topPanel.add(this.searchField);
        this.topPanel.add(this.serversBox);
        this.topPanel.add(this.privateBox);
    }

    private void load(boolean keep){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;

        List<Pack> packs = App.settings.sortPacksAlphabetically() ?
                App.settings.getPacksSortedAlphabetically() :
                App.settings.getPacksSortedPositionally();

        int count = 0;
        for(Pack pack : packs){
            if(pack.canInstall()){
                if(keep){
                    boolean show = true;

                    if(!this.searchField.getText().isEmpty()){
                        if (!Pattern.compile(Pattern.quote(this.searchField.getText()), Pattern.CASE_INSENSITIVE).matcher(pack
                                .getName()).find()) {
                            show = false;
                        }
                    }

                    if(this.serversBox.isSelected()){
                        if (!pack.canCreateServer()) {
                            show = false;
                        }
                    }

                    if(privateBox.isSelected()){
                        if (!pack.isPrivate()) {
                            show = false;
                        }
                    }

                    if(show){
                        this.contentPanel.add(new PackCard(pack), gbc);
                        gbc.gridy++;
                        count++;
                    }
                } else{
                    this.contentPanel.add(new PackCard(pack), gbc);
                    gbc.gridy++;
                    count++;
                }
            }
        }

        if(count == 0){
            this.contentPanel.add(new NilCard(Language.INSTANCE.localize("pack.nodisplay", "\n\n")), gbc);
        }
    }

    public void reload(){
        this.contentPanel.removeAll();
        load(true);
        revalidate();
        repaint();
    }

    @Override
    public String getTitle(){
        return Language.INSTANCE.localize("tabs.packs");
    }
}