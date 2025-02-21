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
package com.atlauncher.gui.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.MCVersionRow;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.gui.components.LockingPreservingCaretTextSetter;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.listener.StatefulTextKeyAdapter;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.viewmodel.base.ICreatePackViewModel;
import com.atlauncher.viewmodel.impl.CreatePackViewModel;
import com.formdev.flatlaf.ui.FlatScrollPaneBorder;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CreatePackTab extends HierarchyPanel implements Tab {
    private JTextField nameField;
    private JTextArea descriptionField;
    private JCheckBox minecraftVersionReleasesFilterCheckbox;
    private JCheckBox minecraftVersionExperimentsFilterCheckbox;
    private JCheckBox minecraftVersionSnapshotsFilterCheckbox;
    private JCheckBox minecraftVersionBetasFilterCheckbox;
    private JCheckBox minecraftVersionAlphasFilterCheckbox;
    private ButtonGroup loaderTypeButtonGroup;
    private JRadioButton loaderTypeNoneRadioButton;
    private JRadioButton loaderTypeFabricRadioButton;
    private JRadioButton loaderTypeForgeRadioButton;
    private JRadioButton loaderTypeLegacyFabricRadioButton;
    private JRadioButton loaderTypeNeoForgeRadioButton;
    private JRadioButton loaderTypePaperRadioButton;
    private JRadioButton loaderTypePurpurRadioButton;
    private JRadioButton loaderTypeQuiltRadioButton;
    private JComboBox<ComboItem<LoaderVersion>> loaderVersionsDropDown;
    private JButton createServerButton;
    private JButton createInstanceButton;
    private ICreatePackViewModel viewModel;
    /**
     * Last time the loaderVersion has been changed.
     * <p>
     * Used to prevent an infinite changes from occuring.
     */
    private long loaderVersionLastChange = System.currentTimeMillis();
    @Nullable
    private JTable minecraftVersionTable = null;
    @Nullable
    private DefaultTableModel minecraftVersionTableModel = null;
    private boolean hasScrolledToSelection = false;

    public CreatePackTab() {
        super(new BorderLayout());
        setName("createPackPanel");
    }

    private String getReleasesText() {
        return GetText.tr("Releases");
    }

    private String getExperimentsText() {
        return GetText.tr("Experiments");
    }

    private String getSnapshotsText() {
        return GetText.tr("Snapshots");
    }

    private String getBetasText() {
        return GetText.tr("Betas");
    }

    private String getAlphasText() {
        return GetText.tr("Alphas");
    }

    private String getNoneText() {
        return GetText.tr("None");
    }

    private String getCreateServerText() {
        return GetText.tr("Create Server");
    }

    private String getCreateInstanceText() {
        return GetText.tr("Create Instance");
    }

    private void setupMainPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel nameLabel = new JLabel(GetText.tr("Instance Name") + ":");
        mainPanel.add(nameLabel, gbc);
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        LockingPreservingCaretTextSetter nameFieldSetter = new LockingPreservingCaretTextSetter(nameField);
        addDisposable(viewModel.name().subscribe((it) -> nameFieldSetter.setText(it.orElse(null))));
        nameField.addKeyListener(new StatefulTextKeyAdapter(
                (e) -> viewModel.setName(nameField.getText()),
                (e) -> nameFieldSetter.setLocked(true),
                (e) -> SwingUtilities.invokeLater(() -> nameFieldSetter.setLocked(false))));
        mainPanel.add(nameField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel descriptionLabel = new JLabel(GetText.tr("Description") + ":");
        mainPanel.add(descriptionLabel, gbc);
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JScrollPane descriptionScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScrollPane.setBorder(new FlatScrollPaneBorder());
        descriptionScrollPane.setPreferredSize(new Dimension(450, 80));
        descriptionScrollPane.setViewportView(descriptionField);

        descriptionField.setLineWrap(true);
        LockingPreservingCaretTextSetter descriptionFieldSetter = new LockingPreservingCaretTextSetter(
                descriptionField);
        addDisposable(viewModel.description().subscribe((it) -> descriptionFieldSetter.setText(it.orElse(null))));
        descriptionField.addKeyListener(new StatefulTextKeyAdapter(
                (e) -> viewModel.setDescription(descriptionField.getText()),
                (e) -> descriptionFieldSetter.setLocked(true),
                (e) -> SwingUtilities.invokeLater(() -> descriptionFieldSetter.setLocked(false))));
        mainPanel.add(descriptionScrollPane, gbc);

        // Minecraft Version
        gbc.gridx = 0;
        gbc.gridy += 2;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JPanel minecraftVersionPanel = new JPanel();
        minecraftVersionPanel.setLayout(new BoxLayout(minecraftVersionPanel, BoxLayout.Y_AXIS));
        JLabel minecraftVersionLabel = new JLabel(GetText.tr("Minecraft Version") + ":");
        minecraftVersionPanel.add(minecraftVersionLabel);
        minecraftVersionPanel.add(Box.createVerticalStrut(20));
        JPanel minecraftVersionFilterPanel = new JPanel();
        minecraftVersionFilterPanel.setLayout(new BoxLayout(minecraftVersionFilterPanel, BoxLayout.Y_AXIS));
        JLabel minecraftVersionFilterLabel = new JLabel(GetText.tr("Filter"));
        addDisposable(viewModel.font().subscribe(minecraftVersionFilterLabel::setFont));
        minecraftVersionFilterPanel.add(minecraftVersionFilterLabel);

        // Release checkbox
        setupReleaseCheckbox(minecraftVersionFilterPanel);

        // Experiments checkbox
        setupExperimentsCheckbox(minecraftVersionFilterPanel);

        // Snapshots checkbox
        setupSnapshotsCheckbox(minecraftVersionFilterPanel);

        // Old Betas checkbox
        setupOldBetasCheckbox(minecraftVersionFilterPanel);

        // Old Alphas checkbox
        setupOldAlphasCheckbox(minecraftVersionFilterPanel);

        minecraftVersionPanel.add(minecraftVersionFilterPanel);
        mainPanel.add(minecraftVersionPanel, gbc);
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        JScrollPane minecraftVersionScrollPane = new JScrollPane(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        minecraftVersionScrollPane.setBorder(new FlatScrollPaneBorder());
        minecraftVersionScrollPane.setPreferredSize(new Dimension(450, 300));
        setupMinecraftVersionsTable();
        minecraftVersionScrollPane.setViewportView(minecraftVersionTable);
        mainPanel.add(minecraftVersionScrollPane, gbc);

        // Loader Type
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel loaderTypeLabel = new JLabel(GetText.tr("Loader") + "?");
        mainPanel.add(loaderTypeLabel, gbc);
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        loaderTypeButtonGroup.add(loaderTypeNoneRadioButton);
        loaderTypeButtonGroup.add(loaderTypeFabricRadioButton);
        loaderTypeButtonGroup.add(loaderTypeForgeRadioButton);
        loaderTypeButtonGroup.add(loaderTypeLegacyFabricRadioButton);
        loaderTypeButtonGroup.add(loaderTypeNeoForgeRadioButton);
        loaderTypeButtonGroup.add(loaderTypePaperRadioButton);
        loaderTypeButtonGroup.add(loaderTypePurpurRadioButton);
        loaderTypeButtonGroup.add(loaderTypeQuiltRadioButton);
        JPanel loaderTypePanel = new JPanel(new FlowLayout());

        setupLoaderNoneButton(loaderTypePanel);
        setupLoaderFabricButton(loaderTypePanel);
        setupLoaderForgeButton(loaderTypePanel);
        setupLoaderLegacyFabricButton(loaderTypePanel);
        setupLoaderNeoForgeButton(loaderTypePanel);
        setupLoaderPaperButton(loaderTypePanel);
        setupLoaderPurpurButton(loaderTypePanel);
        setupLoaderQuiltButton(loaderTypePanel);

        mainPanel.add(loaderTypePanel, gbc);

        // Loader Version
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = UIConstants.LABEL_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        JLabel loaderVersionLabel = new JLabel(GetText.tr("Loader Version") + ":");
        mainPanel.add(loaderVersionLabel, gbc);
        gbc.gridx++;
        gbc.insets = UIConstants.FIELD_INSETS;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        addDisposable(viewModel.loaderVersionsDropDownEnabled().subscribe(loaderVersionsDropDown::setEnabled));

        addDisposable(viewModel.loaderVersions().subscribe((loaderVersionsOptional) -> {
            loaderVersionsDropDown.removeAllItems();
            if (!loaderVersionsOptional.isPresent()) {
                setEmpty();
            } else {
                int loaderVersionLength = 0;

                List<LoaderVersion> loaderVersions = loaderVersionsOptional.get();

                for (LoaderVersion version : loaderVersions) {
                    // ensures that font width is taken into account
                    loaderVersionLength = max(
                            loaderVersionLength,
                            getFontMetrics(App.THEME.getNormalFont())
                                    .stringWidth(version.toString()) + 25);

                    loaderVersionsDropDown.addItem(new ComboItem<>(version, version.toString()));
                }

                // ensures that the dropdown is at least 200 px wide
                loaderVersionLength = max(200, loaderVersionLength);

                // ensures that there is a maximum width of 400 px to prevent overflow
                loaderVersionLength = min(400, loaderVersionLength);
                loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

            }
        }));
        addDisposable(viewModel.selectedLoaderVersionIndex().subscribe((index) -> {
            if (loaderVersionsDropDown.getItemAt(index) != null) {
                loaderVersionLastChange = System.currentTimeMillis();
                loaderVersionsDropDown.setSelectedIndex(index);
            }
        }));
        loaderVersionsDropDown.addActionListener((e) -> {
            // A user cannot change the loader version in under 100 ms. It is physically
            // impossible.
            if (e.getWhen() > (loaderVersionLastChange + 100)) {
                ComboItem<LoaderVersion> comboItem = (ComboItem<LoaderVersion>) loaderVersionsDropDown
                        .getSelectedItem();

                if (comboItem != null) {
                    LoaderVersion version = comboItem.getValue();
                    if (version != null) {
                        viewModel.setLoaderVersion(version);
                    }
                }
            }
        });

        addDisposable(viewModel.loaderLoading().subscribe((it) -> {
            loaderVersionsDropDown.removeAllItems();
            if (it) {
                loaderVersionsDropDown.addItem(new ComboItem<>(null, GetText.tr("Getting Loader Versions")));
            } else {
                setEmpty();
            }
        }));
        mainPanel.add(loaderVersionsDropDown, gbc);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setEmpty() {
        loaderVersionsDropDown.addItem(new ComboItem<>(null, GetText.tr("Select Loader First")));
    }

    private void setupLoaderQuiltButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypeQuiltSelected().subscribe(loaderTypeQuiltRadioButton::setSelected));
        addDisposable(viewModel.loaderTypeQuiltEnabled().subscribe(loaderTypeQuiltRadioButton::setEnabled));
        addDisposable(viewModel.isQuiltVisible().subscribe(loaderTypeQuiltRadioButton::setVisible));
        loaderTypeQuiltRadioButton.addActionListener((e) -> viewModel.setLoaderType(LoaderType.QUILT));
        if (viewModel.showQuiltOption()) {
            loaderTypePanel.add(loaderTypeQuiltRadioButton);
        }
    }

    private void setupLoaderForgeButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypeForgeSelected().subscribe(loaderTypeForgeRadioButton::setSelected));
        addDisposable(viewModel.loaderTypeForgeEnabled().subscribe(loaderTypeForgeRadioButton::setEnabled));
        addDisposable(viewModel.isForgeVisible().subscribe(loaderTypeForgeRadioButton::setVisible));
        loaderTypeForgeRadioButton.addActionListener((e) -> viewModel.setLoaderType(LoaderType.FORGE));
        if (viewModel.showForgeOption()) {
            loaderTypePanel.add(loaderTypeForgeRadioButton);
        }
    }

    private void setupLoaderLegacyFabricButton(JPanel loaderTypePanel) {
        addDisposable(
                viewModel.loaderTypeLegacyFabricSelected().subscribe(loaderTypeLegacyFabricRadioButton::setSelected));
        addDisposable(
                viewModel.loaderTypeLegacyFabricEnabled().subscribe(loaderTypeLegacyFabricRadioButton::setEnabled));
        addDisposable(viewModel.isLegacyFabricVisible().subscribe(loaderTypeLegacyFabricRadioButton::setVisible));
        loaderTypeLegacyFabricRadioButton.addActionListener(
                e -> viewModel.setLoaderType(
                        LoaderType.LEGACY_FABRIC));
        if (viewModel.showLegacyFabricOption()) {
            loaderTypePanel.add(loaderTypeLegacyFabricRadioButton);
        }
    }

    private void setupLoaderNeoForgeButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypeNeoForgeSelected().subscribe(loaderTypeNeoForgeRadioButton::setSelected));
        addDisposable(viewModel.loaderTypeNeoForgeEnabled().subscribe(loaderTypeNeoForgeRadioButton::setEnabled));
        addDisposable(viewModel.isNeoForgeVisible().subscribe(loaderTypeNeoForgeRadioButton::setVisible));
        loaderTypeNeoForgeRadioButton.addActionListener(
                e -> viewModel.setLoaderType(
                        LoaderType.NEOFORGE));
        if (viewModel.showNeoForgeOption()) {
            loaderTypePanel.add(loaderTypeNeoForgeRadioButton);
        }
    }

    private void setupLoaderPaperButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypePaperSelected().subscribe(loaderTypePaperRadioButton::setSelected));
        addDisposable(viewModel.loaderTypePaperEnabled().subscribe(loaderTypePaperRadioButton::setEnabled));
        addDisposable(viewModel.isPaperVisible().subscribe(loaderTypePaperRadioButton::setVisible));
        loaderTypePaperRadioButton.addActionListener(
                e -> viewModel.setLoaderType(LoaderType.PAPER));
        if (viewModel.showPaperOption()) {
            loaderTypePanel.add(loaderTypePaperRadioButton);
        }
    }

    private void setupLoaderPurpurButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypePurpurSelected().subscribe(loaderTypePurpurRadioButton::setSelected));
        addDisposable(viewModel.loaderTypePurpurEnabled().subscribe(loaderTypePurpurRadioButton::setEnabled));
        addDisposable(viewModel.isPurpurVisible().subscribe(loaderTypePurpurRadioButton::setVisible));
        loaderTypePurpurRadioButton.addActionListener(
                e -> viewModel.setLoaderType(LoaderType.PURPUR));
        if (viewModel.showPurpurOption()) {
            loaderTypePanel.add(loaderTypePurpurRadioButton);
        }
    }

    private void setupLoaderFabricButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypeFabricSelected().subscribe(loaderTypeFabricRadioButton::setSelected));
        addDisposable(viewModel.loaderTypeFabricEnabled().subscribe(loaderTypeFabricRadioButton::setEnabled));
        addDisposable(viewModel.isFabricVisible().subscribe(loaderTypeFabricRadioButton::setVisible));
        loaderTypeFabricRadioButton.addActionListener(
                e -> viewModel.setLoaderType(
                        LoaderType.FABRIC));
        if (viewModel.showFabricOption()) {
            loaderTypePanel.add(loaderTypeFabricRadioButton);
        }
    }

    private void setupLoaderNoneButton(JPanel loaderTypePanel) {
        addDisposable(viewModel.loaderTypeNoneSelected().subscribe(loaderTypeNoneRadioButton::setSelected));
        addDisposable(viewModel.loaderTypeNoneEnabled().subscribe(loaderTypeNoneRadioButton::setEnabled));
        loaderTypeNoneRadioButton.addActionListener((e) -> viewModel.setLoaderType(null));
        loaderTypePanel.add(loaderTypeNoneRadioButton);
    }

    private void setupOldAlphasCheckbox(JPanel minecraftVersionFilterPanel) {
        addDisposable(viewModel.oldAlphaSelected().subscribe(minecraftVersionAlphasFilterCheckbox::setSelected));
        addDisposable(viewModel.oldAlphaEnabled().subscribe(minecraftVersionAlphasFilterCheckbox::setEnabled));
        minecraftVersionAlphasFilterCheckbox.addActionListener(
                it -> viewModel.setOldAlphaSelected(minecraftVersionAlphasFilterCheckbox.isSelected()));
        if (viewModel.showOldAlphaOption()) {
            minecraftVersionFilterPanel.add(minecraftVersionAlphasFilterCheckbox);
        }
    }

    private void setupOldBetasCheckbox(JPanel minecraftVersionFilterPanel) {
        addDisposable(viewModel.oldBetaSelected().subscribe(minecraftVersionBetasFilterCheckbox::setSelected));
        addDisposable(viewModel.oldBetaEnabled().subscribe(minecraftVersionBetasFilterCheckbox::setEnabled));
        minecraftVersionBetasFilterCheckbox.addActionListener(
                it -> viewModel.setOldBetaSelected(minecraftVersionBetasFilterCheckbox.isSelected()));
        if (viewModel.showOldBetaOption()) {
            minecraftVersionFilterPanel.add(minecraftVersionBetasFilterCheckbox);
        }
    }

    private void setupSnapshotsCheckbox(JPanel minecraftVersionFilterPanel) {
        addDisposable(viewModel.snapshotSelected().subscribe(minecraftVersionSnapshotsFilterCheckbox::setSelected));
        addDisposable(viewModel.snapshotEnabled().subscribe(minecraftVersionSnapshotsFilterCheckbox::setEnabled));
        minecraftVersionSnapshotsFilterCheckbox.addActionListener(
                it -> viewModel.setSnapshotSelected(minecraftVersionSnapshotsFilterCheckbox.isSelected()));
        if (viewModel.showSnapshotOption()) {
            minecraftVersionFilterPanel.add(minecraftVersionSnapshotsFilterCheckbox);
        }
    }

    private void setupExperimentsCheckbox(JPanel minecraftVersionFilterPanel) {
        addDisposable(viewModel.experimentSelected().subscribe(minecraftVersionExperimentsFilterCheckbox::setSelected));
        addDisposable(viewModel.experimentEnabled().subscribe(minecraftVersionExperimentsFilterCheckbox::setEnabled));
        minecraftVersionExperimentsFilterCheckbox.addActionListener(
                it -> viewModel.setExperimentSelected(minecraftVersionExperimentsFilterCheckbox.isSelected()));
        if (viewModel.showExperimentOption()) {
            minecraftVersionFilterPanel.add(minecraftVersionExperimentsFilterCheckbox);
        }
    }

    private void setupReleaseCheckbox(JPanel minecraftVersionFilterPanel) {
        addDisposable(viewModel.releaseSelected().subscribe(minecraftVersionReleasesFilterCheckbox::setSelected));
        addDisposable(viewModel.releaseEnabled().subscribe(minecraftVersionReleasesFilterCheckbox::setEnabled));
        minecraftVersionReleasesFilterCheckbox.setSelected(true);
        minecraftVersionReleasesFilterCheckbox.addActionListener(
                it -> viewModel.setReleaseSelected(minecraftVersionReleasesFilterCheckbox.isSelected()));
        if (viewModel.showReleaseOption()) {
            minecraftVersionFilterPanel.add(minecraftVersionReleasesFilterCheckbox);
        }
    }

    @SuppressWarnings("null")
	private void setupMinecraftVersionsTable() {
        minecraftVersionTableModel = new DefaultTableModel(
                new String[][] {},
                new String[] { GetText.tr("Version"), GetText.tr("Released"), GetText.tr("Type") }) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }
        };

        minecraftVersionTable = new JTable(minecraftVersionTableModel);
        minecraftVersionTable.getTableHeader().setReorderingAllowed(false);
        ListSelectionModel sm = minecraftVersionTable.getSelectionModel();
        sm.addListSelectionListener((e) -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();

            int minIndex = e.getFirstIndex();
            int maxIndex = e.getLastIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) {
                    viewModel.setSelectedMinecraftVersion(
                            (String) minecraftVersionTableModel.getValueAt(i, 0));
                }
            }
        });

        BehaviorSubject<Boolean> isTableSetup = BehaviorSubject.createDefault(false);

        addDisposable(viewModel.minecraftVersions().subscribe((minecraftVersions) -> {
            // remove all rows
            int rowCount = 0;
            if (minecraftVersionTableModel != null) {
                rowCount = minecraftVersionTableModel.getRowCount();
            }

            if (rowCount > 0) {
                for (int i = rowCount - 1; i >= 0; i--) {
                    if (minecraftVersionTableModel != null) {
                        minecraftVersionTableModel.removeRow(i);
                    }
                }
            }

            for (MCVersionRow row : minecraftVersions) {
                if (minecraftVersionTableModel != null) {
                    minecraftVersionTableModel.addRow(
                            new Object[] {
                                    row.id,
                                    row.date,
                                    row.type
                            });
                }
            }

            // refresh the table
            if (minecraftVersionTable != null) {
                minecraftVersionTable.revalidate();
            }
            isTableSetup.onNext(true);
        }));

        addDisposable(Observable.combineLatest(
                isTableSetup.filter(setup -> setup),
                viewModel.selectedMinecraftVersionIndex(),
                (setup, index) -> index).subscribe(it -> {
                    if (minecraftVersionTable != null) {
                        int rowCount = minecraftVersionTable.getRowCount();

                        if (it < rowCount) {
                            minecraftVersionTable.setRowSelectionInterval(it, it);
                            minecraftVersionTable.revalidate();

                            if (!hasScrolledToSelection) {
                                Rectangle rect = minecraftVersionTable.getCellRect(it, 0, true);
                                minecraftVersionTable.scrollRectToVisible(rect);
                                hasScrolledToSelection = true;
                            }
                        }
                    }
                }));

        TableColumnModel cm = minecraftVersionTable.getColumnModel();
        cm.getColumn(0).setResizable(false);
        cm.getColumn(1).setResizable(false);
        cm.getColumn(1).setMaxWidth(200);
        cm.getColumn(2).setResizable(false);
        cm.getColumn(2).setMaxWidth(200);
        minecraftVersionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        minecraftVersionTable.setShowVerticalLines(false);
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(createServerButton);
        createServerButton.addActionListener((event) -> { // user has no instances, they may not be aware this is not
                                                          // how to play
            if (viewModel.warnUserAboutServer()) {
                int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Are you sure you want to create a server?"))
                        .setContent(
                                new HTMLBuilder().center().text(
                                        GetText.tr(
                                                "Creating a server won't allow you play Minecraft, it's for letting others play together.<br/><br/>If you just want to play Minecraft, you don't want to create a server, and instead will want to create an instance.<br/><br/>Are you sure you want to create a server?"))
                                        .build())
                        .setType(DialogManager.QUESTION).show();
                if (ret != 0) {
                    return;
                }
            }
            viewModel.createServer();
        });
        addDisposable(viewModel.createInstanceDisabledReason()
                .subscribe((reason) -> createInstanceButton.setToolTipText(reason.orElse(null))));
        addDisposable(viewModel.createInstanceEnabled().subscribe(createInstanceButton::setEnabled));
        addDisposable(viewModel.createServerEnabled().subscribe(createServerButton::setEnabled));
        bottomPanel.add(createInstanceButton);
        createInstanceButton.addActionListener((event) -> viewModel.createInstance());
        add(bottomPanel, BorderLayout.SOUTH);
    }

    @Override
    public String getTitle() {
        return GetText.tr("Create Pack");
    }

    @Override
    public String getAnalyticsScreenViewName() {
        return "Create Pack";
    }

    @Override
    protected void createViewModel() {
        viewModel = new CreatePackViewModel();
    }

    @Override
    protected void onShow() {
        hasScrolledToSelection = false;
        nameField = new JTextField(32);
        descriptionField = new JTextArea(2, 40);
        minecraftVersionReleasesFilterCheckbox = new JCheckBox(getReleasesText());
        minecraftVersionExperimentsFilterCheckbox = new JCheckBox(getExperimentsText());
        minecraftVersionSnapshotsFilterCheckbox = new JCheckBox(getSnapshotsText());
        minecraftVersionBetasFilterCheckbox = new JCheckBox(getBetasText());
        minecraftVersionAlphasFilterCheckbox = new JCheckBox(getAlphasText());
        loaderTypeButtonGroup = new ButtonGroup();
        loaderTypeNoneRadioButton = new JRadioButton(getNoneText());
        loaderTypeFabricRadioButton = new JRadioButton("Fabric");
        loaderTypeForgeRadioButton = new JRadioButton("Forge");
        loaderTypeLegacyFabricRadioButton = new JRadioButton("Legacy Fabric");
        loaderTypeNeoForgeRadioButton = new JRadioButton("NeoForge");
        loaderTypePaperRadioButton = new JRadioButton("Paper");
        loaderTypePurpurRadioButton = new JRadioButton("Purpur");
        loaderTypeQuiltRadioButton = new JRadioButton("Quilt");
        loaderVersionsDropDown = new JComboBox<>();
        createServerButton = new JButton(getCreateServerText());
        createInstanceButton = new JButton(getCreateInstanceText());

        setupMainPanel();
        setupBottomPanel();
    }

    @Override
    protected void onDestroy() {
        removeAll();
        nameField = null;
        descriptionField = null;
        minecraftVersionReleasesFilterCheckbox = null;
        minecraftVersionExperimentsFilterCheckbox = null;
        minecraftVersionSnapshotsFilterCheckbox = null;
        minecraftVersionBetasFilterCheckbox = null;
        minecraftVersionAlphasFilterCheckbox = null;
        loaderTypeButtonGroup = null;
        loaderTypeNoneRadioButton = null;
        loaderTypeFabricRadioButton = null;
        loaderTypeForgeRadioButton = null;
        loaderTypeLegacyFabricRadioButton = null;
        loaderTypeNeoForgeRadioButton = null;
        loaderTypePaperRadioButton = null;
        loaderTypePurpurRadioButton = null;
        loaderTypeQuiltRadioButton = null;
        loaderVersionsDropDown = null;
        createServerButton = null;
        createInstanceButton = null;
    }
}