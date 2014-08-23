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

import com.atlauncher.App;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.ConsoleCloseListener;
import com.atlauncher.evnt.listener.ConsoleOpenListener;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.ConsoleCloseManager;
import com.atlauncher.evnt.manager.ConsoleOpenManager;
import com.atlauncher.evnt.manager.RelocalizationManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public final class TrayMenu
        extends JPopupMenu
        implements RelocalizationListener,
                   ConsoleCloseListener,
                   ConsoleOpenListener{

    private final JMenuItem killMCButton = new JMenuItem();
    private final JMenuItem tcButton = new JMenuItem();
    private final JMenuItem quitButton = new JMenuItem();

    public TrayMenu(){
        super();

        this.setMinecraftLaunched(false);

        this.killMCButton.setText("Kill Minecraft");
        this.tcButton.setText("Toggle Console");
        this.quitButton.setText("Quit");

        this.tcButton.setEnabled(false);

        this.add(this.killMCButton);
        this.add(this.tcButton);
        this.addSeparator();
        this.add(this.quitButton);

        ConsoleCloseManager.addListener(this);
        ConsoleOpenManager.addListener(this);
        RelocalizationManager.addListener(this);

        this.addActionListeners();
    }

    private void addActionListeners(){
        this.killMCButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                SwingUtilities.invokeLater(new Runnable(){
                    @Override
                    public void run(){
                        if(App.settings.isMinecraftLaunched()){
                            int ret = JOptionPane.showConfirmDialog(
                                    App.settings.getParent(),
                                    "<html><p align=\"center\">"
                                            + App.settings.getLocalizedString(
                                            "console.killsure", "<br/><br/>")
                                            + "</p></html>", App.settings
                                            .getLocalizedString("console.kill"),
                                    JOptionPane.YES_OPTION);

                            if(ret == JOptionPane.YES_OPTION){
                                App.settings.killMinecraft();
                            }
                        }
                    }
                });
            }
        });
        this.tcButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                App.settings.getConsole().setVisible(!App.settings.getConsole().isVisible());
            }
        });
        this.quitButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                System.exit(0);
            }
        });
    }

    public void localize(){
        this.tcButton.setEnabled(true);
        if(App.settings.isConsoleVisible()){
            this.tcButton.setText(Language.INSTANCE.localize("console.hide"));
        } else{
            this.tcButton.setText(Language.INSTANCE.localize("console.show"));
        }

        this.killMCButton.setText(Language.INSTANCE.localize("console.kill"));
        this.quitButton.setText(Language.INSTANCE.localize("common.quit"));
    }

    public void setMinecraftLaunched(boolean l){
        this.killMCButton.setEnabled(l);
    }

    @Override
    public void onConsoleClose(){
        this.tcButton.setText(Language.INSTANCE.localize("console.show"));
    }

    @Override
    public void onConsoleOpen(){
        this.tcButton.setText(Language.INSTANCE.localize("console.hide"));
    }

    @Override
    public void onRelocalization(){
        this.killMCButton.setText(Language.INSTANCE.localize("console.kill"));
        this.quitButton.setText(Language.INSTANCE.localize("common.quit"));
        if(App.settings.getConsole().isVisible()){
            this.tcButton.setText(Language.INSTANCE.localize("console.hide"));
        } else{
            this.tcButton.setText(Language.INSTANCE.localize("console.show"));
        }
    }
}
