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
package com.atlauncher.gui.tabs

import com.atlauncher.App
import com.atlauncher.builders.HTMLBuilder
import com.atlauncher.constants.UIConstants
import com.atlauncher.data.minecraft.loaders.LoaderType
import com.atlauncher.data.minecraft.loaders.LoaderVersion
import com.atlauncher.managers.*
import com.atlauncher.utils.ComboItem
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel
import com.atlauncher.viewmodel.impl.VanillaPacksViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mini2Dx.gettext.GetText
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.*
import javax.swing.event.ListSelectionListener
import javax.swing.table.DefaultTableModel
import kotlin.math.max
import kotlin.math.min

class VanillaPacksTab : JPanel(BorderLayout()), Tab {
    private val nameField = JTextField(32)
    private val descriptionField = JTextArea(2, 40)
    private val minecraftVersionReleasesFilterCheckbox = JCheckBox(GetText.tr("Releases"))
    private val minecraftVersionExperimentsFilterCheckbox = JCheckBox(GetText.tr("Experiments"))
    private val minecraftVersionSnapshotsFilterCheckbox = JCheckBox(GetText.tr("Snapshots"))
    private val minecraftVersionBetasFilterCheckbox = JCheckBox(GetText.tr("Betas"))
    private val minecraftVersionAlphasFilterCheckbox = JCheckBox(GetText.tr("Alphas"))
    private var minecraftVersionTable: JTable? = null
    private var minecraftVersionTableModel: DefaultTableModel? = null
    private val loaderTypeButtonGroup = ButtonGroup()
    private val loaderTypeNoneRadioButton = JRadioButton(GetText.tr("None"))
    private val loaderTypeFabricRadioButton = JRadioButton("Fabric")
    private val loaderTypeForgeRadioButton = JRadioButton("Forge")
    private val loaderTypeQuiltRadioButton = JRadioButton("Quilt")
    private val loaderVersionsDropDown = JComboBox<ComboItem<LoaderVersion?>>()
    private val createServerButton = JButton(GetText.tr("Create Server"))
    private val createInstanceButton = JButton(GetText.tr("Create Instance"))
    private val viewModel: IVanillaPacksViewModel = VanillaPacksViewModel()
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        name = "vanillaPacksPanel"
        setupMainPanel()
        setupBottomPanel()
    }

    private fun setupMainPanel() {
        val mainPanel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        // Name
        gbc.gridx = 0
        gbc.gridy = 0
        gbc.insets = UIConstants.LABEL_INSETS
        gbc.anchor = GridBagConstraints.EAST
        val nameLabel = JLabel(GetText.tr("Instance Name") + ":")
        mainPanel.add(nameLabel, gbc)
        gbc.gridx++
        gbc.insets = UIConstants.FIELD_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_LEADING
        scope.launch {
            viewModel.name.collect {
                nameField.text = it
            }
        }
        nameField.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                viewModel.setName(nameField.text)
            }
        })
        mainPanel.add(nameField, gbc)

        // Description
        gbc.gridx = 0
        gbc.gridy++
        gbc.insets = UIConstants.LABEL_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING
        val descriptionLabel = JLabel(GetText.tr("Description") + ":")
        mainPanel.add(descriptionLabel, gbc)
        gbc.gridx++
        gbc.insets = UIConstants.FIELD_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_LEADING
        val descriptionScrollPane = JScrollPane(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        descriptionScrollPane.preferredSize = Dimension(450, 80)
        descriptionScrollPane.setViewportView(descriptionField)

        scope.launch {
            viewModel.description.collect {
                descriptionField.text = it
            }
        }
        descriptionField.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(e: KeyEvent?) {
                viewModel.setDescription(descriptionField.text)
            }
        })
        mainPanel.add(descriptionScrollPane, gbc)

        // Minecraft Version
        gbc.gridx = 0
        gbc.gridy += 2
        gbc.insets = UIConstants.LABEL_INSETS
        gbc.anchor = GridBagConstraints.NORTHEAST
        val minecraftVersionPanel = JPanel()
        minecraftVersionPanel.layout = BoxLayout(minecraftVersionPanel, BoxLayout.Y_AXIS)
        val minecraftVersionLabel = JLabel(GetText.tr("Minecraft Version") + ":")
        minecraftVersionPanel.add(minecraftVersionLabel)
        minecraftVersionPanel.add(Box.createVerticalStrut(20))
        val minecraftVersionFilterPanel = JPanel()
        minecraftVersionFilterPanel.layout = BoxLayout(minecraftVersionFilterPanel, BoxLayout.Y_AXIS)
        val minecraftVersionFilterLabel = JLabel(GetText.tr("Filter"))
        scope.launch {
            viewModel.font.collect {
                minecraftVersionFilterLabel.font = it
            }
        }
        minecraftVersionFilterPanel.add(minecraftVersionFilterLabel)

        // Release checkbox
        setupReleaseCheckbox(minecraftVersionFilterPanel)

        // Experiments checkbox
        setupExperimentsCheckbox(minecraftVersionFilterPanel)

        // Snapshots checkbox
        setupSnapshotsCheckbox(minecraftVersionFilterPanel)

        // Old Betas checkbox
        setupOldBetasCheckbox(minecraftVersionFilterPanel)

        // Old Alphas checkbox
        setupOldAlphasCheckbox(minecraftVersionFilterPanel)

        minecraftVersionPanel.add(minecraftVersionFilterPanel)
        mainPanel.add(minecraftVersionPanel, gbc)
        gbc.gridx++
        gbc.insets = UIConstants.FIELD_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_LEADING
        val minecraftVersionScrollPane = JScrollPane(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        )
        minecraftVersionScrollPane.preferredSize = Dimension(450, 300)
        setupMinecraftVersionsTable()
        minecraftVersionScrollPane.setViewportView(minecraftVersionTable)
        mainPanel.add(minecraftVersionScrollPane, gbc)

        // Loader Type
        gbc.gridx = 0
        gbc.gridy++
        gbc.insets = UIConstants.LABEL_INSETS
        gbc.anchor = GridBagConstraints.EAST
        val loaderTypeLabel = JLabel(GetText.tr("Loader") + "?")
        mainPanel.add(loaderTypeLabel, gbc)
        gbc.gridx++
        gbc.insets = UIConstants.FIELD_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_LEADING
        loaderTypeButtonGroup.add(loaderTypeNoneRadioButton)
        loaderTypeButtonGroup.add(loaderTypeFabricRadioButton)
        loaderTypeButtonGroup.add(loaderTypeForgeRadioButton)
        loaderTypeButtonGroup.add(loaderTypeQuiltRadioButton)
        val loaderTypePanel = JPanel(FlowLayout())

        setupLoaderNoneButton(loaderTypePanel)

        setupLoaderFabricButton(loaderTypePanel)

        setupLoaderForgeButton(loaderTypePanel)

        setupLoaderQuiltButton(loaderTypePanel)

        mainPanel.add(loaderTypePanel, gbc)

        // Loader Version
        gbc.gridx = 0
        gbc.gridy++
        gbc.insets = UIConstants.LABEL_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING
        val loaderVersionLabel = JLabel(GetText.tr("Loader Version") + ":")
        mainPanel.add(loaderVersionLabel, gbc)
        gbc.gridx++
        gbc.insets = UIConstants.FIELD_INSETS
        gbc.anchor = GridBagConstraints.BASELINE_LEADING
        scope.launch {
            viewModel.loaderVersionsDropDownEnabled.collect {
                loaderVersionsDropDown.isEnabled = it
            }
        }
        scope.launch {
            viewModel.loaderVersions.collect { loaderVersions ->
                loaderVersionsDropDown.removeAllItems()
                if (loaderVersions == null) {
                    setEmpty()
                } else {
                    var loaderVersionLength = 0

                    loaderVersions.forEach { version ->
                        // ensures that font width is taken into account
                        loaderVersionLength = max(
                            loaderVersionLength,
                            getFontMetrics(App.THEME.normalFont)
                                .stringWidth(version.toString()) + 25
                        )

                        loaderVersionsDropDown.addItem(
                            ComboItem(
                                version,
                                version.version
                            )
                        )
                    }

                    if (viewModel.selectedLoaderType.value == LoaderType.FORGE) {
                        val recommendedVersion =
                            loaderVersions.firstOrNull { lv: LoaderVersion -> lv.recommended }
                        if (recommendedVersion != null) {
                            loaderVersionsDropDown.selectedIndex =
                                loaderVersions.indexOf(recommendedVersion)
                        }
                    }

                    // ensures that the dropdown is at least 200 px wide
                    loaderVersionLength = max(200, loaderVersionLength)

                    // ensures that there is a maximum width of 400 px to prevent overflow
                    loaderVersionLength = min(400, loaderVersionLength)
                    loaderVersionsDropDown.preferredSize = Dimension(loaderVersionLength, 23)

                }
            }
        }
        scope.launch {
            viewModel.loaderLoading.collect {
                loaderVersionsDropDown.removeAllItems()
                if (it) {
                    loaderVersionsDropDown.addItem(ComboItem(null, GetText.tr("Getting Loader Versions")))
                } else {
                    setEmpty()
                }
            }
        }
        mainPanel.add(loaderVersionsDropDown, gbc)
        add(mainPanel, BorderLayout.CENTER)
    }

    private fun setEmpty() {
        loaderVersionsDropDown.addItem(ComboItem(null, GetText.tr("Select Loader First")))

    }

    private fun setupLoaderQuiltButton(loaderTypePanel: JPanel) {
        scope.launch {
            viewModel.loaderTypeQuiltSelected.collect {
                loaderTypeQuiltRadioButton.isSelected = it
            }
        }
        scope.launch {
            viewModel.loaderTypeQuiltEnabled.collect {
                loaderTypeQuiltRadioButton.isEnabled = it
            }
        }
        loaderTypeQuiltRadioButton.addActionListener { e: ActionEvent? ->
            viewModel.setLoaderType(
                LoaderType.QUILT
            )
        }
        if (viewModel.showQuiltOption) {
            loaderTypePanel.add(loaderTypeQuiltRadioButton)
        }
    }

    private fun setupLoaderForgeButton(loaderTypePanel: JPanel) {
        scope.launch {
            viewModel.loaderTypeForgeSelected.collect {
                loaderTypeForgeRadioButton.isSelected = it
            }
        }
        scope.launch {
            viewModel.loaderTypeForgeEnabled.collect {
                loaderTypeForgeRadioButton.isEnabled = it
            }
        }
        loaderTypeForgeRadioButton.addActionListener { e: ActionEvent? ->
            viewModel.setLoaderType(
                LoaderType.FORGE
            )
        }
        if (viewModel.showForgeOption) {
            loaderTypePanel.add(loaderTypeForgeRadioButton)
        }
    }

    private fun setupLoaderFabricButton(loaderTypePanel: JPanel) {
        scope.launch {
            viewModel.loaderTypeFabricSelected.collect {
                loaderTypeFabricRadioButton.isSelected = it
            }
        }
        scope.launch {
            viewModel.loaderTypeFabricEnabled.collect {
                loaderTypeFabricRadioButton.isEnabled = it
            }
        }
        loaderTypeFabricRadioButton.addActionListener { e: ActionEvent? ->
            viewModel.setLoaderType(
                LoaderType.FABRIC
            )
        }
        if (viewModel.showFabricOption) {
            loaderTypePanel.add(loaderTypeFabricRadioButton)
        }
    }

    private fun setupLoaderNoneButton(loaderTypePanel: JPanel) {
        scope.launch {
            viewModel.loaderTypeNoneSelected.collect {
                loaderTypeNoneRadioButton.isSelected = it
            }
        }
        scope.launch {
            viewModel.loaderTypeNoneEnabled.collect {
                loaderTypeNoneRadioButton.isEnabled = it
            }
        }
        loaderTypeNoneRadioButton.addActionListener { e: ActionEvent? ->
            viewModel.setLoaderType(null)
        }
        loaderTypePanel.add(loaderTypeNoneRadioButton)
    }

    private fun setupOldAlphasCheckbox(minecraftVersionFilterPanel: JPanel) {
        scope.launch {
            viewModel.oldAlphaSelected.collect {
                minecraftVersionAlphasFilterCheckbox.isSelected = it
            }
        }
        scope.launch {
            viewModel.oldAlphaEnabled.collect {
                minecraftVersionAlphasFilterCheckbox.isEnabled = it
            }
        }
        minecraftVersionAlphasFilterCheckbox.addActionListener {
            viewModel.setOldAlphaSelected(minecraftVersionAlphasFilterCheckbox.isSelected)
        }
        if (viewModel.showOldAlphaOption) {
            minecraftVersionFilterPanel.add(minecraftVersionAlphasFilterCheckbox)
        }
    }

    private fun setupOldBetasCheckbox(minecraftVersionFilterPanel: JPanel) {
        scope.launch {
            viewModel.oldBetaSelected.collect {
                minecraftVersionBetasFilterCheckbox.isSelected = it
            }
        }
        scope.launch {
            viewModel.oldBetaEnabled.collect {
                minecraftVersionBetasFilterCheckbox.isEnabled = it
            }
        }
        minecraftVersionBetasFilterCheckbox.addActionListener {
            viewModel.setOldBetaSelected(minecraftVersionBetasFilterCheckbox.isSelected)
        }
        if (viewModel.showOldBetaOption) {
            minecraftVersionFilterPanel.add(minecraftVersionBetasFilterCheckbox)
        }
    }

    private fun setupSnapshotsCheckbox(minecraftVersionFilterPanel: JPanel) {
        scope.launch {
            viewModel.snapshotSelected.collect {
                minecraftVersionSnapshotsFilterCheckbox.isSelected = it
            }
        }
        scope.launch {
            viewModel.snapshotEnabled.collect {
                minecraftVersionSnapshotsFilterCheckbox.isEnabled = it
            }
        }
        minecraftVersionSnapshotsFilterCheckbox.addActionListener {
            viewModel.setSnapshotSelected(minecraftVersionSnapshotsFilterCheckbox.isSelected)
        }
        if (viewModel.showSnapshotOption) {
            minecraftVersionFilterPanel.add(minecraftVersionSnapshotsFilterCheckbox)
        }
    }

    private fun setupExperimentsCheckbox(minecraftVersionFilterPanel: JPanel) {
        scope.launch {
            viewModel.experimentSelected.collect {
                minecraftVersionExperimentsFilterCheckbox.isSelected = it
            }
        }
        scope.launch {
            viewModel.experimentEnabled.collect {
                minecraftVersionExperimentsFilterCheckbox.isEnabled = it
            }
        }
        minecraftVersionExperimentsFilterCheckbox.addActionListener {
            viewModel.setExperimentSelected(minecraftVersionExperimentsFilterCheckbox.isSelected)
        }
        if (viewModel.showExperimentOption) {
            minecraftVersionFilterPanel.add(minecraftVersionExperimentsFilterCheckbox)
        }
    }

    private fun setupReleaseCheckbox(minecraftVersionFilterPanel: JPanel) {
        scope.launch {
            viewModel.releaseSelected.collect {
                minecraftVersionReleasesFilterCheckbox.isSelected = it
            }
        }
        scope.launch {
            viewModel.releaseEnabled.collect {
                minecraftVersionReleasesFilterCheckbox.isEnabled = it
            }
        }
        minecraftVersionReleasesFilterCheckbox.isSelected = true
        minecraftVersionReleasesFilterCheckbox.addActionListener {
            viewModel.setReleaseSelected(minecraftVersionReleasesFilterCheckbox.isSelected)
        }
        if (viewModel.showReleaseOption) {
            minecraftVersionFilterPanel.add(minecraftVersionReleasesFilterCheckbox)
        }
    }

    private fun setupMinecraftVersionsTable() {
        minecraftVersionTableModel = object : DefaultTableModel(
            arrayOf<Array<String>>(), arrayOf(GetText.tr("Version"), GetText.tr("Released"), GetText.tr("Type"))
        ) {
            override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
                return false
            }
        }

        minecraftVersionTable = JTable(minecraftVersionTableModel)
        minecraftVersionTable!!.tableHeader.reorderingAllowed = false
        val sm = minecraftVersionTable!!.selectionModel
        sm.addListSelectionListener(ListSelectionListener { e ->
            if (e.valueIsAdjusting) {
                return@ListSelectionListener
            }
            val lsm = e.source as ListSelectionModel
            val minIndex = lsm.minSelectionIndex
            val maxIndex = lsm.maxSelectionIndex
            for (i in minIndex until maxIndex) {
                if (lsm.isSelectedIndex(i)) {
                    viewModel.setSelectedMinecraftVersion(
                        minecraftVersionTableModel!!.getValueAt(
                            i, 0
                        ) as String
                    )
                }
            }
        })
        scope.launch {
            viewModel.minecraftVersions.collect { minecraftVersions ->

                // remove all rows
                val rowCount = minecraftVersionTableModel!!.rowCount
                if (rowCount > 0) {
                    for (i in rowCount - 1 downTo 0) {
                        minecraftVersionTableModel!!.removeRow(i)
                    }
                }

                minecraftVersions.forEach { row: IVanillaPacksViewModel.MCVersionRow ->
                    minecraftVersionTableModel!!.addRow(
                        arrayOf(
                            row.id,
                            row.date,
                            row.type
                        )
                    )
                }
                /*
                if (minecraftVersionTable!!.rowCount >= 1) {
                    // figure out which row to select
                    var newSelectedRow = 0
                    if (selectedMinecraftVersion != null) {
                        val versionToSelect = minecraftVersions.stream()
                            .filter { mv: VersionManifestVersion -> mv.id == selectedMinecraftVersion }.findFirst()
                        if (versionToSelect.isPresent) {
                            newSelectedRow = minecraftVersions.indexOf(versionToSelect.get())
                        }
                    }
                    minecraftVersionTable!!.setRowSelectionInterval(newSelectedRow, newSelectedRow)
                }
                 */

                // refresh the table
                minecraftVersionTable!!.revalidate()
            }
        }

        val cm = minecraftVersionTable!!.columnModel
        cm.getColumn(0).resizable = false
        cm.getColumn(1).resizable = false
        cm.getColumn(1).maxWidth = 200
        cm.getColumn(2).resizable = false
        cm.getColumn(2).maxWidth = 200
        minecraftVersionTable!!.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        minecraftVersionTable!!.showVerticalLines = false
    }

    private fun setupBottomPanel() {
        val bottomPanel = JPanel(FlowLayout())
        bottomPanel.add(createServerButton)
        createServerButton.addActionListener(ActionListener { // user has no instances, they may not be aware this is not how to play
            if (viewModel.warnUserAboutServer) {
                val ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Are you sure you want to create a server?"))
                    .setContent(
                        HTMLBuilder().center().text(
                            GetText.tr(
                                "Creating a server won't allow you play Minecraft, it's for letting others play together.<br/><br/>If you just want to play Minecraft, you don't want to create a server, and instead will want to create an instance.<br/><br/>Are you sure you want to create a server?"
                            )
                        ).build()
                    ).setType(DialogManager.QUESTION).show()
                if (ret != 0) {
                    return@ActionListener
                }
            }
            viewModel.createServer()
        })
        bottomPanel.add(createInstanceButton)
        createInstanceButton.addActionListener { viewModel.createInstance() }
        add(bottomPanel, BorderLayout.SOUTH)
    }

    override fun getTitle(): String {
        return GetText.tr("Vanilla Packs")
    }

    override fun getAnalyticsScreenViewName(): String {
        return "Vanilla Packs"
    }
}