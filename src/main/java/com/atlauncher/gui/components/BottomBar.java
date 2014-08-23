/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.components;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.gui.CustomLineBorder;
import com.atlauncher.utils.Utils;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public abstract class BottomBar extends JPanel{
    /**
     * Auto generated serial.
     */
    private static final long serialVersionUID = -7488195680365431776L;
    protected JButton creeperHostIcon;
    protected JButton facebookIcon;
    protected JButton githubIcon;
    protected JButton twitterIcon;
    protected JButton redditIcon;

    protected JPanel rightSide;

    public BottomBar(){
        this.setupSocialButtons();
        this.setupSocialButtonListeners();

        rightSide = new JPanel();
        rightSide.setLayout(new FlowLayout());

        rightSide.add(creeperHostIcon);
        rightSide.add(facebookIcon);
        rightSide.add(githubIcon);
        rightSide.add(redditIcon);
        rightSide.add(twitterIcon);
    }

    private void setupSocialButtons(){
        creeperHostIcon = new JButton(Utils.getIconImage("/assets/image/CreeperHostIcon.png")){
            public JToolTip createToolTip(){
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        creeperHostIcon.setBorder(BorderFactory.createEmptyBorder());
        creeperHostIcon.setContentAreaFilled(false);
        creeperHostIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        creeperHostIcon
                .setToolTipText("CreeperHost - Minecraft servers for ATLauncher packs and more");

        facebookIcon = new JButton(Utils.getIconImage("/assets/image/FacebookIcon.png")){
            public JToolTip createToolTip(){
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        facebookIcon.setBorder(BorderFactory.createEmptyBorder());
        facebookIcon.setContentAreaFilled(false);
        facebookIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        facebookIcon.setToolTipText("Facebook");

        githubIcon = new JButton(Utils.getIconImage("/assets/image/GitHubIcon.png")){
            public JToolTip createToolTip(){
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        githubIcon.setBorder(BorderFactory.createEmptyBorder());
        githubIcon.setContentAreaFilled(false);
        githubIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        githubIcon.setToolTipText("GitHub");

        redditIcon = new JButton(Utils.getIconImage("/assets/image/RedditIcon.png")){
            public JToolTip createToolTip(){
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        redditIcon.setBorder(BorderFactory.createEmptyBorder());
        redditIcon.setContentAreaFilled(false);
        redditIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        redditIcon.setToolTipText("Reddit");

        twitterIcon = new JButton(Utils.getIconImage("/assets/image/TwitterIcon.png")){
            public JToolTip createToolTip(){
                JToolTip tip = super.createToolTip();
                Border border = new CustomLineBorder(5, App.THEME.getHoverBorderColor(), 2);
                tip.setBorder(border);
                return tip;
            }
        };
        twitterIcon.setBorder(BorderFactory.createEmptyBorder());
        twitterIcon.setContentAreaFilled(false);
        twitterIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        twitterIcon.setToolTipText("Twitter");
    }

    private void setupSocialButtonListeners(){
        creeperHostIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                LogManager.info("Opening Up CreeperHost");
                Utils.openBrowser("http://billing.creeperhost.net/link.php?id=7");
            }
        });
        facebookIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                LogManager.info("Opening Up ATLauncher Facebook Page");
                Utils.openBrowser("http://www.facebook.com/ATLauncher");
            }
        });
        githubIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                LogManager.info("Opening Up ATLauncher GitHub Page");
                Utils.openBrowser("https://github.com/ATLauncher/ATLauncher");
            }
        });
        redditIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                LogManager.info("Opening Up ATLauncher Reddit Page");
                Utils.openBrowser("http://www.reddit.com/r/ATLauncher");
            }
        });
        twitterIcon.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                LogManager.info("Opening Up ATLauncher Twitter Page");
                Utils.openBrowser("http://www.twitter.com/ATLauncher");
            }
        });
    }

}