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
package com.atlauncher.gui.components;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.ModPlatform;
import com.atlauncher.data.Type;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.CurseForgeSocialLinkType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.modrinth.ModrinthDonationUrl;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.gui.HoverLineBorder;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

/**
 * This class extends {@link JCheckBox} and overrides the need to use JCheckBox
 * in the {@link ModsChooser}, {@link ModsChooser} and {@link EditModsDialog},
 * providing specific functionality for those two components. Mainly providing a
 * hover tooltip for a mods description, as well as giving pack developers a way
 * to colour mod's names.
 */
public class ModsJCheckBox extends JCheckBox {
    /**
     * The mod this object will use to display it's data. Will be type {@link Mod},
     * {@link com.atlauncher.data.json.Mod} or {@link DisableableMod}.
     */
    private final Object mod;

    private final EditModsDialog dialog;

    /**
     * Constructor for use in the {@link ModsChooser} dialog with new JSON format.
     *
     * @param mod
     *            The mod this object is displaying data for
     */
    public ModsJCheckBox(Mod mod, EditModsDialog dialog) {
        super(mod.getName());

        if (mod.hasColour() && mod.getCompiledColour() != null) {
            setForeground(mod.getCompiledColour());
        }

        this.mod = mod;
        this.dialog = dialog;

        if (mod.hasDescription()) {
            this.setToolTipText(new HTMLBuilder().text(mod.getDescription()).split(100).build());
        }
    }

    public ModsJCheckBox(Mod mod) {
        this(mod, null);
    }

    /**
     * Constructor for use in the {@link EditModsDialog} dialog.
     *
     * @param mod
     *            The mod this object is displaying data for
     */
    public ModsJCheckBox(DisableableMod mod, EditModsDialog dialog) {
        super(mod.type == Type.plugins ? "[Plugin] " + mod.getName() : mod.getName());

        if (mod.hasColour()) {
            setForeground(mod.getColour());
        }

        this.mod = mod;
        this.dialog = dialog;

        if (mod.getDescription() != null && !mod.getDescription().isEmpty()) {
            this.setToolTipText(new HTMLBuilder().text(mod.getDescription()).split(100).build());
        }

        if (this.dialog != null) {
            setupContextMenu();
        }
    }

    public ModsJCheckBox(DisableableMod mod) {
        this(mod, null);
    }

    /**
     * Gets the {@link Mod} object associated with this.
     *
     * @return The mod for this object
     */
    public Mod getMod() {
        return (Mod) this.mod;
    }

    /**
     * Gets the {@link DisableableMod} object associated with this.
     *
     * @return The mod for this object
     */
    public DisableableMod getDisableableMod() {
        return (DisableableMod) this.mod;
    }

    private void setupContextMenu() {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem fileItem = new JMenuItem(getDisableableMod().file);
        fileItem.setEnabled(false);
        contextMenu.add(fileItem);
        contextMenu.add(new JPopupMenu.Separator());

        if (getDisableableMod().hasFullCurseForgeInformation()) {
            CurseForgeProject curseForgeProject = getDisableableMod().curseForgeProject;

            // #. {0} is the platform to open the website for (e.g. CurseForge/Modrinth)
            JMenuItem openOnCurseForge = new JMenuItem(GetText.tr("Open On {0}", "CurseForge"));
            openOnCurseForge
                    .addActionListener(e -> OS.openWebBrowser(curseForgeProject.getWebsiteUrl()));
            contextMenu.add(openOnCurseForge);

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.WEBSITE)) {
                JMenuItem openWebsite = new JMenuItem(GetText.tr("Open Website"));
                openWebsite.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.WEBSITE)));
                contextMenu.add(openWebsite);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.GITHUB)) {
                JMenuItem openGitHub = new JMenuItem(GetText.tr("Open GitHub"));
                openGitHub.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.GITHUB)));
                contextMenu.add(openGitHub);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.DISCORD)) {
                JMenuItem openDiscord = new JMenuItem(GetText.tr("Open Discord"));
                openDiscord.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.DISCORD)));
                contextMenu.add(openDiscord);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.MASTODON)) {
                JMenuItem openMastodon = new JMenuItem(GetText.tr("Open Mastodon"));
                openMastodon.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.MASTODON)));
                contextMenu.add(openMastodon);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.FACEBOOK)) {
                JMenuItem openFacebook = new JMenuItem(GetText.tr("Open Facebook"));
                openFacebook.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.FACEBOOK)));
                contextMenu.add(openFacebook);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.TWITTER)) {
                JMenuItem openTwitter = new JMenuItem(GetText.tr("Open Twitter"));
                openTwitter.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.TWITTER)));
                contextMenu.add(openTwitter);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.INSTAGRAM)) {
                JMenuItem openInstagram = new JMenuItem(GetText.tr("Open Instagram"));
                openInstagram.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.INSTAGRAM)));
                contextMenu.add(openInstagram);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.PATREON)) {
                JMenuItem openPatreon = new JMenuItem(GetText.tr("Open Patreon"));
                openPatreon.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.PATREON)));
                contextMenu.add(openPatreon);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.TWITCH)) {
                JMenuItem openTwitch = new JMenuItem(GetText.tr("Open Twitch"));
                openTwitch.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.TWITCH)));
                contextMenu.add(openTwitch);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.REDDIT)) {
                JMenuItem openReddit = new JMenuItem(GetText.tr("Open Reddit"));
                openReddit.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.REDDIT)));
                contextMenu.add(openReddit);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.YOUTUBE)) {
                JMenuItem openYouTube = new JMenuItem(GetText.tr("Open YouTube"));
                openYouTube.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.YOUTUBE)));
                contextMenu.add(openYouTube);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.TIKTOK)) {
                JMenuItem openTikTok = new JMenuItem(GetText.tr("Open TikTok"));
                openTikTok.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.TIKTOK)));
                contextMenu.add(openTikTok);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.PINTEREST)) {
                JMenuItem openPinterest = new JMenuItem(GetText.tr("Open Pinterest"));
                openPinterest.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.PINTEREST)));
                contextMenu.add(openPinterest);
            }

            if (curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.BLUESKY)) {
                JMenuItem openBlueSky = new JMenuItem(GetText.tr("Open BlueSky"));
                openBlueSky.addActionListener(
                        e -> OS.openWebBrowser(curseForgeProject.getSocialLink(CurseForgeSocialLinkType.BLUESKY)));
                contextMenu.add(openBlueSky);
            }

            contextMenu.add(new JPopupMenu.Separator());
        }

        if (getDisableableMod().isFromModrinth()) {
            ModrinthProject modrinthMod = getDisableableMod().modrinthProject;

            // #. {0} is the platform to open the website for (e.g. CurseForge/Modrinth)
            JMenuItem openOnModrinth = new JMenuItem(GetText.tr("Open On {0}", "Modrinth"));
            openOnModrinth.addActionListener(
                    e -> OS.openWebBrowser(String.format("https://modrinth.com/mod/%s", modrinthMod.slug)));
            contextMenu.add(openOnModrinth);

            if (modrinthMod.discordUrl != null) {
                JMenuItem openDiscord = new JMenuItem(GetText.tr("Open Discord"));
                openDiscord.addActionListener(e -> OS.openWebBrowser(modrinthMod.discordUrl));
                contextMenu.add(openDiscord);
            }

            if (modrinthMod.issuesUrl != null) {
                JMenuItem openIssues = new JMenuItem(GetText.tr("Open Issues"));
                openIssues.addActionListener(e -> OS.openWebBrowser(modrinthMod.issuesUrl));
                contextMenu.add(openIssues);
            }

            if (modrinthMod.sourceUrl != null) {
                JMenuItem openSourceUrl = new JMenuItem(GetText.tr("Open Source Url"));
                openSourceUrl.addActionListener(e -> OS.openWebBrowser(modrinthMod.sourceUrl));
                contextMenu.add(openSourceUrl);
            }

            if (modrinthMod.wikiUrl != null) {
                JMenuItem openWiki = new JMenuItem(GetText.tr("Open Wiki"));
                openWiki.addActionListener(e -> OS.openWebBrowser(modrinthMod.wikiUrl));
                contextMenu.add(openWiki);
            }

            contextMenu.add(new JPopupMenu.Separator());

            if (modrinthMod.donationUrls != null && !modrinthMod.donationUrls.isEmpty()) {
                for (ModrinthDonationUrl donation : modrinthMod.donationUrls) {
                    // #. {0} is the name of the platform used for donations (Patreon, paypal, etc)
                    JMenuItem openDonationLink = new JMenuItem(GetText.tr("Donate ({0})", donation.platform));
                    openDonationLink.addActionListener(e -> OS.openWebBrowser(donation.url));
                    contextMenu.add(openDonationLink);
                }

                contextMenu.add(new JPopupMenu.Separator());
            }
        }

        JMenuItem enableDisableButton = new JMenuItem(
                getDisableableMod().disabled ? GetText.tr("Enable") : GetText.tr("Disable"));
        enableDisableButton.addActionListener(e -> {
            if (getDisableableMod().disabled) {
                getDisableableMod().enable(dialog.instanceOrServer);
            } else {
                getDisableableMod().disable(dialog.instanceOrServer);
            }

            dialog.reloadPanels();
        });
        contextMenu.add(enableDisableButton);

        contextMenu.add(new JPopupMenu.Separator());

        JMenuItem showInFileExplorer = new JMenuItem(GetText.tr("Show In File Explorer"));
        showInFileExplorer.addActionListener(e -> {
            if (getDisableableMod().disabled) {
                OS.openFileExplorer(getDisableableMod().getDisabledFile(dialog.instanceOrServer).toPath());
            } else {
                OS.openFileExplorer(getDisableableMod().getFile(dialog.instanceOrServer).toPath());
            }
        });
        contextMenu.add(showInFileExplorer);

        contextMenu.add(new JPopupMenu.Separator());

        JMenuItem remove = new JMenuItem(GetText.tr("Remove"));
        remove.addActionListener(e -> {
            dialog.instanceOrServer.getMods().remove(getDisableableMod());
            Utils.delete(
                    (getDisableableMod().isDisabled() ? getDisableableMod().getDisabledFile(dialog.instanceOrServer)
                            : getDisableableMod().getFile(dialog.instanceOrServer)));

            dialog.reloadPanels();
        });
        contextMenu.add(remove);

        if (getDisableableMod().isFromCurseForge() && getDisableableMod().isFromModrinth()) {
            contextMenu.add(new JPopupMenu.Separator());

            // #. {0} is the platform to reinstall the mod from (e.g. CurseForge/Modrinth)
            JMenuItem reinstallFromCurseForge = new JMenuItem(GetText.tr("Reinstall From {0}", "CurseForge"));
            reinstallFromCurseForge.addActionListener(e -> {
                getDisableableMod().reinstall(dialog, dialog.instanceOrServer, ModPlatform.CURSEFORGE);

                dialog.reloadPanels();
            });
            contextMenu.add(reinstallFromCurseForge);

            // #. {0} is the platform to reinstall the mod from (e.g. CurseForge/Modrinth)
            JMenuItem reinstallFromModrinth = new JMenuItem(GetText.tr("Reinstall From {0}", "Modrinth"));
            reinstallFromModrinth.addActionListener(e -> {
                getDisableableMod().reinstall(dialog, dialog.instanceOrServer, ModPlatform.MODRINTH);

                dialog.reloadPanels();
            });
            contextMenu.add(reinstallFromModrinth);

            contextMenu.add(new JPopupMenu.Separator());

            // #. {0} is the platform to check for updates from (e.g. CurseForge/Modrinth)
            JMenuItem checkForUpdatesOnCurseForge = new JMenuItem(GetText.tr("Check For Updates On {0}", "CurseForge"));
            checkForUpdatesOnCurseForge.addActionListener(e -> {
                boolean updated = getDisableableMod().checkForUpdate(dialog, dialog.instanceOrServer,
                        ModPlatform.CURSEFORGE);

                if (!updated) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Updates Found"))
                            .setContent(GetText.tr("No updates were found.")).show();
                }

                dialog.reloadPanels();
            });
            contextMenu.add(checkForUpdatesOnCurseForge);

            // #. {0} is the platform to check for updates from (e.g. CurseForge/Modrinth)
            JMenuItem checkForUpdatesOnModrinth = new JMenuItem(GetText.tr("Check For Updates On {0}", "Modrinth"));
            checkForUpdatesOnModrinth.addActionListener(e -> {
                boolean updated = getDisableableMod().checkForUpdate(dialog, dialog.instanceOrServer,
                        ModPlatform.MODRINTH);

                if (!updated) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Updates Found"))
                            .setContent(GetText.tr("No updates were found.")).show();
                }

                dialog.reloadPanels();
            });
            contextMenu.add(checkForUpdatesOnModrinth);
        } else if (getDisableableMod().isFromCurseForge() || getDisableableMod().isFromModrinth()) {
            contextMenu.add(new JPopupMenu.Separator());

            JMenuItem reinstall = new JMenuItem(GetText.tr("Reinstall"));
            reinstall.addActionListener(e -> {
                getDisableableMod().reinstall(dialog, dialog.instanceOrServer);

                dialog.reloadPanels();
            });
            contextMenu.add(reinstall);

            JMenuItem checkForUpdates = new JMenuItem(GetText.tr("Check For Updates"));
            checkForUpdates.addActionListener(e -> {
                boolean updated = getDisableableMod().checkForUpdate(dialog, dialog.instanceOrServer);

                if (!updated) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Updates Found"))
                            .setContent(GetText.tr("No updates were found.")).show();
                }

                dialog.reloadPanels();
            });
            contextMenu.add(checkForUpdates);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    contextMenu.show(ModsJCheckBox.this, e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBorder(new HoverLineBorder());
        return tip;
    }

}
