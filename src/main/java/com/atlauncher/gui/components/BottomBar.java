/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt.
 */
package com.atlauncher.gui.components;

import com.atlauncher.LogManager;
import com.atlauncher.utils.Utils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class BottomBar extends JPanel {
    private static final long serialVersionUID = -7488195680365431776L;

    protected final JButton creeperHostIcon = new SMButton("/assets/image/CreeperHostIcon.png",
            "CreeperHost - Minecraft servers for ATLauncher packs & more");
    protected final JButton facebookIcon = new SMButton("/assets/image/FacebookIcon.png", "Facebook");
    protected final JButton githubIcon = new SMButton("/assets/image/GitHubIcon.png", "GitHub");
    protected final JButton twitterIcon = new SMButton("/assets/image/TwitterIcon.png", "Twitter");
    protected final JButton redditIcon = new SMButton("/assets/image/RedditIcon.png", "Reddit");

    protected final JPanel rightSide = new JPanel(new FlowLayout());

    public BottomBar() {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEtchedBorder());
        this.setPreferredSize(new Dimension(0, 50));
        this.add(this.rightSide, BorderLayout.EAST);
        this.setupSocialButtonListeners();
        this.rightSide.add(this.creeperHostIcon);
        this.rightSide.add(this.facebookIcon);
        this.rightSide.add(this.githubIcon);
        this.rightSide.add(this.redditIcon);
        this.rightSide.add(this.twitterIcon);
    }

    private void setupSocialButtonListeners() {
        creeperHostIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogManager.info("Opening Up CreeperHost");
                Utils.openBrowser("http://billing.creeperhost.net/link.php?id=7");
            }
        });
        facebookIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogManager.info("Opening Up ATLauncher Facebook Page");
                Utils.openBrowser("http://www.facebook.com/ATLauncher");
            }
        });
        githubIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogManager.info("Opening Up ATLauncher GitHub Page");
                Utils.openBrowser("https://github.com/ATLauncher/ATLauncher");
            }
        });
        redditIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogManager.info("Opening Up ATLauncher Reddit Page");
                Utils.openBrowser("http://www.reddit.com/r/ATLauncher");
            }
        });
        twitterIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                LogManager.info("Opening Up ATLauncher Twitter Page");
                Utils.openBrowser("http://www.twitter.com/ATLauncher");
            }
        });
    }
}