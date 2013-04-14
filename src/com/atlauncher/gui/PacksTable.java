/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.gui;

import java.awt.Color;
import java.awt.Cursor;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.atlauncher.data.Pack;
import com.atlauncher.data.Packs;
import com.atlauncher.data.Player;
import com.atlauncher.data.Version;

public class PacksTable extends JTable {

    public PacksTable() {

        Version[] versions = { new Version(1, 1, 0), new Version(1, 1, 1),
                new Version(1, 1, 2) };
        Pack astockyPack = new Pack(
                1,
                "Astocky Pack",
                new Player("astocky"),
                versions,
                "Astocky Pack is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack herocraftReloaded = new Pack(
                2,
                "HeroCraft Reloaded",
                new Player("dwinget2008"),
                versions,
                "HeroCraft Reloaded is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack solitaryCraft = new Pack(
                3,
                "SolitaryCraft",
                new Player("haighyorkie"),
                versions,
                "SolitaryCraft is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Pack theAllmightyPack = new Pack(
                4,
                "The Allmighty Pack",
                new Player("RyanTheAllmighty"),
                versions,
                "The Allmighty Pack is a pack which does stuff, you know Minecraft and stuff!",
                "Hi");
        Packs packs = new Packs();
        packs.addPack(astockyPack);
        packs.addPack(herocraftReloaded);
        packs.addPack(solitaryCraft);
        packs.addPack(theAllmightyPack);

        setModel(new PackTableModel(packs));
        setRowHeight(50);
        setSelectionBackground(Color.GRAY);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setResizingAllowed(false);
        getColumnModel().getColumn(getColumnCount() - 1).setMinWidth(400);
    }

    public Pack getSelectedPack() {
        return (Pack) getValueAt(getSelectedRow(), -1);
    }

}
