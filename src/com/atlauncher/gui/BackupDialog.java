package com.atlauncher.gui;

import com.atlauncher.App;
import com.atlauncher.data.Instance;
import com.atlauncher.data.SyncAbstract;
import com.atlauncher.utils.Utils;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Kihira
 */
public class BackupDialog extends JDialog implements ActionListener{

    private final Instance instance;
    private final JButton backupButton = new JButton(App.settings.getLocalizedString("common.backup"));
    private final JButton restoreButton = new JButton(App.settings.getLocalizedString("common.restore"));
    private final JButton deleteButton = new JButton(App.settings.getLocalizedString("common.delete"));
    private JList worldList;
    private JList backupList;
    private SyncAbstract selectedSync = SyncAbstract.syncList.get(App.settings.getLastSelectedSync());

    public BackupDialog(Instance inst) {
        super(App.settings.getParent(), App.settings.getLocalizedString("backup.dialog.title"));

        instance = inst;

        setSize(320, 420);
        setLocationRelativeTo(App.settings.getParent());
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        requestFocus();

        JPanel backupPanel = createBackupPanel();
        JPanel restorePanel = createRestorePanel();
        JPanel settingsPanel = createSettingsPanel();
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(App.settings.getLocalizedString("common.backup"), null, backupPanel, App.settings.getLocalizedString("backup.tab.backup.tooltip"));
        tabbedPane.addTab(App.settings.getLocalizedString("common.restore"), null, restorePanel, App.settings.getLocalizedString("backup.tab.restore.tooltip"));
        tabbedPane.addTab(App.settings.getLocalizedString("tabs.settings"), null, settingsPanel, App.settings.getLocalizedString("backup.tab.settings.tooltip"));
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //Update the backup/restore lists
                List<String> list = selectedSync.getBackupsForInstance(instance);
                if (list == null) backupList.setListData(new String[0]);
                else backupList.setListData(list.toArray(new String[list.size()]));
                list = new ArrayList<String>();
                if (instance.getSavesDirectory().exists()) {
                    if (instance.getSavesDirectory().exists()) {
                        File[] files = instance.getSavesDirectory().listFiles();
                        if (files != null) {
                            for (File file:files) {
                                if ((file.isDirectory()) && (!file.getName().equals("NEI"))) list.add(file.getName());
                            }
                        }
                    }
                }
                if (list.size() == 0) worldList.setListData(new String[0]);
                else worldList.setListData(list.toArray(new String[list.size()]));
            }
        });

        add(tabbedPane, BorderLayout.NORTH);
    }

    private JPanel createBackupPanel() {
        List<String> worldData = new ArrayList<String>();
        if (instance.getSavesDirectory().exists()) {
            if (instance.getSavesDirectory().exists()) {
                File[] files = instance.getSavesDirectory().listFiles();
                if (files != null) {
                    for (File file:files) {
                        if ((file.isDirectory()) && (!file.getName().equals("NEI"))) worldData.add(file.getName());
                    }
                }
            }
        }

        worldList = new JList(worldData.toArray(new String[worldData.size()]));
        worldList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        worldList.setLayoutOrientation(JList.VERTICAL_WRAP);
        worldList.setVisibleRowCount(-1);
        worldList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        worldList.setFixedCellWidth(305);
        worldList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    backupButton.doClick();
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(worldList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setPreferredSize(new Dimension(230, 300));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        JLabel backupLabel = new JLabel(App.settings.getLocalizedString("backup.label.backupchoose"));
        backupLabel.setLabelFor(worldList);
        backupLabel.setHorizontalAlignment(SwingConstants.CENTER);
        backupLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        backupButton.setHorizontalAlignment(SwingConstants.CENTER);
        backupButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(backupButton);

        JPanel backupPanel = new JPanel();
        backupPanel.setLayout(new BorderLayout());
        backupPanel.add(backupLabel, BorderLayout.NORTH);
        backupPanel.add(listScroller, BorderLayout.CENTER);
        backupPanel.add(buttonPanel, BorderLayout.SOUTH);

        return backupPanel;
    }

    private JPanel createRestorePanel() {
        JComboBox syncChoice = new JComboBox();
        for (Map.Entry<String, SyncAbstract> entry:SyncAbstract.syncList.entrySet()) {
            syncChoice.addItem(entry.getKey());
        }
        syncChoice.addActionListener(this);
        syncChoice.setMaximumSize(new Dimension(100, 50));

        List<String> list = selectedSync.getBackupsForInstance(instance);
        if (list == null) backupList = new JList();
        else backupList = new JList(list.toArray(new String[list.size()]));
        backupList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        backupList.setLayoutOrientation(JList.VERTICAL_WRAP);
        backupList.setVisibleRowCount(-1);
        backupList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        backupList.setFixedCellWidth(305);
        backupList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    restoreButton.doClick();
                }
            }
        });

        JScrollPane listScroller = new JScrollPane(backupList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroller.setPreferredSize(new Dimension(230, 300));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        JLabel restoreLabel = new JLabel(App.settings.getLocalizedString("backup.label.restorechoose"));
        restoreLabel.setLabelFor(backupList);
        restoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        restoreLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerSize(0);
        splitPane.setRightComponent(syncChoice);
        splitPane.setLeftComponent(restoreLabel);

        restoreButton.addActionListener(this);
        deleteButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(restoreButton);
        buttonPanel.add(deleteButton);

        JPanel restorePanel = new JPanel();
        restorePanel.setLayout(new BorderLayout());
        restorePanel.add(splitPane, BorderLayout.NORTH);
        restorePanel.add(listScroller, BorderLayout.CENTER);
        restorePanel.add(buttonPanel, BorderLayout.SOUTH);

        return restorePanel;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.add(new BackupGeneralSettingsPanel());

        for (Map.Entry<String, SyncAbstract> entry:SyncAbstract.syncList.entrySet()) {
            CollapsiblePanel settingPanel = entry.getValue().getSettingsPanel();
            if (settingPanel != null) settingsPanel.add(settingPanel);
        }
        return settingsPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (("Backup".equals(e.getActionCommand())) && (worldList.getSelectedValue() != null))  {
            String worldToBackup = (String) worldList.getSelectedValue();
            String backupName = JOptionPane.showInputDialog(this, App.settings.getLocalizedString("backup.message.backupname"),
                    App.settings.getLocalizedString("backup.message.backupname.title"), JOptionPane.QUESTION_MESSAGE);
            if (backupName != null) {
                for (Map.Entry<String, SyncAbstract> entry:SyncAbstract.syncList.entrySet()) {
                    File worldData = new File(instance.getSavesDirectory(), worldToBackup);
                    if (worldData.exists()) entry.getValue().backupWorld(backupName, worldData, instance);
                    else JOptionPane.showMessageDialog(this, App.settings.getLocalizedString("backup.message.backupfailed.missingdirectory"),
                            App.settings.getLocalizedString("backup.message.backupfailed.title"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        else if (("Restore".equals(e.getActionCommand())) && (backupList.getSelectedValue() != null))  {
            String backupToRestore = (String) backupList.getSelectedValue();
            selectedSync.restoreBackup(backupToRestore, instance);
        }
        else if (("Delete".equals(e.getActionCommand())) && (backupList.getSelectedValue() != null))  {
            String backupToDelete = (String) backupList.getSelectedValue();
            if (JOptionPane.showOptionDialog(this, App.settings.getLocalizedString("backup.message.deleteconfirm", backupToDelete),
                    App.settings.getLocalizedString("backup.message.deleteconfirm.title"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                selectedSync.deleteBackup(backupToDelete, instance);
                //Update the backup list
                List<String> list = selectedSync.getBackupsForInstance(instance);
                if (list == null) backupList.setListData(new String[0]);
                else backupList.setListData(list.toArray(new String[list.size()]));
            }
        }
        else if (e.getSource() instanceof JComboBox) {
            String selection = (String) ((JComboBox) e.getSource()).getSelectedItem();
            selectedSync = SyncAbstract.syncList.get(selection);
            App.settings.setLastSelectedSync(selection);
            List<String> list = selectedSync.getBackupsForInstance(instance);
            backupList.setListData(list.toArray(new String[list.size()]));
        }
    }

    public class BackupGeneralSettingsPanel extends CollapsiblePanel {

        private final GridBagConstraints gbc = new GridBagConstraints();

        public BackupGeneralSettingsPanel() {
            super("General");
            JPanel panel = super.getContentPane();
            panel.setLayout(new GridBagLayout());
            ImageIcon helpIcon = Utils.getIconImage("/resources/Help.png");

            panel.setMinimumSize(new Dimension(300, 10));
            panel.setSize(300, 300);

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(3, 0, 3, 10);
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;

            JLabel autoBackupLabel = new JLabel(App.settings.getLocalizedString("backup.label.autobackup") + ":");
            autoBackupLabel.setIcon(helpIcon);
            autoBackupLabel.setToolTipText(App.settings.getLocalizedString("backup.label.autobackup.tooltip"));
            panel.add(autoBackupLabel, gbc);

            JCheckBox autoBackup = new JCheckBox();
            autoBackup.setToolTipText(App.settings.getLocalizedString("backup.label.autobackup.tooltip"));
            autoBackup.setSelected(App.settings.getAutoBackup());
            autoBackup.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    App.settings.setAutoBackup(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            panel.add(autoBackup, getGBCForField());

            JLabel notifyLabel = new JLabel(App.settings.getLocalizedString("backup.label.notify") + ":");
            notifyLabel.setIcon(helpIcon);
            notifyLabel.setToolTipText(App.settings.getLocalizedString("backup.label.notify.tooltip"));
            panel.add(notifyLabel, getGBCForLabel());

            JCheckBox notify = new JCheckBox();
            notify.setToolTipText(App.settings.getLocalizedString("backup.label.notify.tooltip"));
            notify.setSelected(App.settings.getNotifyBackup());
            notify.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    App.settings.setNotifyBackup(e.getStateChange() == ItemEvent.SELECTED);
                }
            });
            panel.add(notify, getGBCForField());
        }

        private GridBagConstraints getGBCForLabel() {
            gbc.gridx = 0;
            gbc.gridy++;
            gbc.insets = new Insets(3, 0, 3, 10);
            gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
            return gbc;
        }

        private GridBagConstraints getGBCForField() {
            gbc.gridx++;
            gbc.insets = new Insets(3, 0, 3, 0);;
            gbc.anchor = GridBagConstraints.BASELINE_LEADING;
            return gbc;
        }
    }
}
