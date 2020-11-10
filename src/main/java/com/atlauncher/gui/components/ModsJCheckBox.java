/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.json.Mod;
import com.atlauncher.gui.HoverLineBorder;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import org.mini2Dx.gettext.GetText;

/**
 * This class extends {@link JCheckBox} and overrides the need to use JCheckBox
 * in the {@link ModsChooser}, {@link ModsChooser} and {@link EditModsDialog},
 * providing specific functionality for those two components. Mainly providing a
 * hover tooltip for a mods description, as well as giving pack developers a way
 * to colour mod's names.
 */
@SuppressWarnings("serial")
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
     * @param mod The mod this object is displaying data for
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
     * @param mod The mod this object is displaying data for
     */
    public ModsJCheckBox(DisableableMod mod, EditModsDialog dialog) {
        super(mod.getName());

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

        if (getDisableableMod().hasFullCurseInformation()) {
            JMenuItem openOnCurse = new JMenuItem(GetText.tr("Open On Curse"));
            openOnCurse.addActionListener(e -> OS.openWebBrowser(getDisableableMod().curseMod.websiteUrl));
            contextMenu.add(openOnCurse);

            contextMenu.add(new JPopupMenu.Separator());
        }

        JMenuItem enableDisableButton = new JMenuItem(
                getDisableableMod().disabled ? GetText.tr("Enable") : GetText.tr("Disable"));
        enableDisableButton.addActionListener(e -> {
            if (dialog.instance != null) {
                if (getDisableableMod().disabled) {
                    getDisableableMod().enable(dialog.instance);
                } else {
                    getDisableableMod().disable(dialog.instance);
                }
            } else if (dialog.instanceV2 != null) {
                if (getDisableableMod().disabled) {
                    getDisableableMod().enable(dialog.instanceV2);
                } else {
                    getDisableableMod().disable(dialog.instanceV2);
                }
            }

            dialog.reloadPanels();
        });
        contextMenu.add(enableDisableButton);

        contextMenu.add(new JPopupMenu.Separator());

        JMenuItem showInFileExplorer = new JMenuItem(GetText.tr("Show In File Explorer"));
        showInFileExplorer.addActionListener(e -> {
            if (dialog.instance != null) {
                if (getDisableableMod().disabled) {
                    OS.openFileExplorer(getDisableableMod().getDisabledFile(dialog.instance).toPath());
                } else {
                    OS.openFileExplorer(getDisableableMod().getFile(dialog.instance).toPath());
                }
            } else if (dialog.instanceV2 != null) {
                if (getDisableableMod().disabled) {
                    OS.openFileExplorer(getDisableableMod().getDisabledFile(dialog.instanceV2).toPath());
                } else {
                    OS.openFileExplorer(getDisableableMod().getFile(dialog.instanceV2).toPath());
                }
            }
        });
        contextMenu.add(showInFileExplorer);

        contextMenu.add(new JPopupMenu.Separator());

        JMenuItem remove = new JMenuItem(GetText.tr("Remove"));
        remove.addActionListener(e -> {
            if (dialog.instance != null) {
                dialog.instance.removeInstalledMod(getDisableableMod());
            } else if (dialog.instanceV2 != null) {
                dialog.instanceV2.launcher.mods.remove(getDisableableMod());
                Utils.delete((getDisableableMod().isDisabled() ? getDisableableMod().getDisabledFile(dialog.instanceV2)
                        : getDisableableMod().getFile(dialog.instanceV2)));
            }

            dialog.reloadPanels();
        });
        contextMenu.add(remove);

        if (getDisableableMod().isFromCurse()) {
            contextMenu.add(new JPopupMenu.Separator());

            JMenuItem reinstall = new JMenuItem(GetText.tr("Reinstall"));
            reinstall.addActionListener(e -> {
                if (dialog.instance != null) {
                    getDisableableMod().reinstall(dialog.instance);
                } else if (dialog.instanceV2 != null) {
                    getDisableableMod().reinstall(dialog.instanceV2);
                }

                dialog.reloadPanels();
            });
            contextMenu.add(reinstall);

            JMenuItem checkForUpdates = new JMenuItem(GetText.tr("Check For Updates"));
            checkForUpdates.addActionListener(e -> {
                boolean updated = false;

                if (dialog.instance != null) {
                    updated = getDisableableMod().checkForUpdate(dialog.instance);
                } else if (dialog.instanceV2 != null) {
                    updated = getDisableableMod().checkForUpdate(dialog.instanceV2);
                }

                if (!updated) {
                    DialogManager.okDialog().setTitle(GetText.tr("No Updates Found"))
                            .setContent(GetText.tr("No updates were found.")).show();
                }

                dialog.reloadPanels();
            });
            contextMenu.add(checkForUpdates);
        }

        addMouseListener(new MouseAdapter() {
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
