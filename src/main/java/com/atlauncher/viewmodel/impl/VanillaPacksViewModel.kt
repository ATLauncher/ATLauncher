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
package com.atlauncher.viewmodel.impl

import com.atlauncher.App
import com.atlauncher.data.installables.Installable
import com.atlauncher.data.installables.VanillaInstallable
import com.atlauncher.data.minecraft.VersionManifestVersion
import com.atlauncher.data.minecraft.VersionManifestVersionType
import com.atlauncher.data.minecraft.loaders.LoaderType
import com.atlauncher.data.minecraft.loaders.LoaderVersion
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader
import com.atlauncher.evnt.listener.SettingsListener
import com.atlauncher.evnt.manager.SettingsManager
import com.atlauncher.exceptions.InvalidMinecraftVersion
import com.atlauncher.gui.tabs.VanillaPacksTab
import com.atlauncher.managers.ConfigManager
import com.atlauncher.managers.MinecraftManager
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel.MCVersionRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.apache.logging.log4j.LogManager
import java.awt.Font
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

/**
 * 25 / 06 / 2022
 */
class VanillaPacksViewModel : IVanillaPacksViewModel, SettingsListener {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Contains a cache of loader versions
     */
    private val loaderVersionsMap = HashMap<LoaderType, List<LoaderVersion>?>()

    /**
     * Filters applied to the version table
     */
    private val minecraftVersionTypeFiltersFlow =
        MutableStateFlow(
            mapOf(
                VersionManifestVersionType.RELEASE to true
            )
        )

    /**
     * Name is dirty, as the user has inputted something
     */
    private var nameDirty = false

    /**
     * Name to display
     */
    private val nameFlow = MutableStateFlow<String?>(null)

    /**
     * Description is dirty, as the user has inputted something
     */
    private var descriptionDirty = false

    /**
     * Description to display
     */
    private val descriptionFlow = MutableStateFlow<String?>(null)

    private val selectedMinecraftVersionFlow = MutableStateFlow<String?>(null)
    private val selectedLoaderTypeFlow = MutableStateFlow<LoaderType?>(null)
    private val selectedLoaderVersionFlow = MutableStateFlow<LoaderVersion?>(null)

    /**
     * If the loader group is enabled or not to be modified
     */
    private val loaderGroupEnabledFlow = MutableStateFlow(false)
    private val loaderVersionsLoadingFlow = MutableStateFlow(false)

    private val fontFlow = MutableStateFlow<Font>(App.THEME.boldFont)

    // Consumers
    /**
     * Represents the version list
     */
    private val versionsFlow by lazy {
        minecraftVersionTypeFiltersFlow.map { versionFilter: Map<VersionManifestVersionType, Boolean> ->
            val filtered = versionFilter.filter { it.value }.map { it.key }.toList()

            MinecraftManager
                .getFilteredMinecraftVersions(filtered)
                .map { it: VersionManifestVersion -> MCVersionRow(it.id, it.releaseTime, it.id) }
                .toTypedArray()
        }
    }


    /**
     * Presents the loader versions for the user to select
     */
    private val loaderVersions: Flow<Array<LoaderVersion>?> by lazy {
        selectedLoaderTypeFlow.combine(selectedMinecraftVersionFlow) { loaderType, version ->
            LOG.debug("loaderVersions map")
            if (loaderType == null) return@combine null
            if (version == null) return@combine null
            loaderGroupEnabledFlow.value = false
            loaderVersionsLoadingFlow.value = true
            var loaderVersions: List<LoaderVersion>? = loaderVersionsMap[loaderType]
            if (loaderVersions == null) {
                loaderVersions =
                    when (loaderType) {
                        LoaderType.FABRIC -> FabricLoader.getChoosableVersions(version)
                        LoaderType.FORGE -> ForgeLoader.getChoosableVersions(version)
                        LoaderType.QUILT -> QuiltLoader.getChoosableVersions(version)
                    }
                loaderVersionsMap[loaderType] = loaderVersions
            }
            loaderVersionsLoadingFlow.value = false
            loaderGroupEnabledFlow.value = true
            if (loaderVersions!!.isEmpty()) return@combine null
            loaderVersions.toTypedArray()
        }
    }

    private val createServerVisibleObservable by lazy {
        selectedMinecraftVersionFlow
            .map { selectedVersion ->
                if (selectedVersion != null) try {
                    val version =
                        MinecraftManager.getMinecraftVersion(selectedVersion)
                    return@map version.hasServer()
                } catch (ignored: InvalidMinecraftVersion) {
                }
                false
            }
    }

    private val fabricVisibleFlow by lazy {
        selectedMinecraftVersionFlow
            .map { version -> !fabricDisabledMCVersions.contains(version) }
    }

    private val forgeVisibleFlow by lazy {
        selectedMinecraftVersionFlow
            .map { version -> !forgeDisabledMCVersions.contains(version) }
    }

    private val quiltVisibleFlow by lazy {
        selectedMinecraftVersionFlow
            .map { version -> !quiltDisabledMCVersions.contains(version) }
    }

    init {
        SettingsManager.addListener(this)
        scope.launch {
            selectedMinecraftVersionFlow.collect { version ->
                if (version != null) {
                    loaderGroupEnabledFlow.value = true
                    val defaultNameFieldValue = String.format(
                        "Minecraft %s",
                        version
                    )
                    if (nameFlow.value == null || nameFlow.value!!.isEmpty() || !nameDirty) {
                        nameDirty = false
                        nameFlow.value = (defaultNameFieldValue)
                    }
                    if (descriptionFlow.value == null || descriptionFlow.value!!.isEmpty() || !descriptionDirty) {
                        descriptionDirty = false
                        descriptionFlow.value = (defaultNameFieldValue)
                    }
                } else {
                    loaderGroupEnabledFlow.value = false
                }
            }
        }
    }

    private fun <K, V> Map<K, V>.copy(): Map<K, V> {
        val map = HashMap<K, V>()
        this.forEach { (k, v) ->
            map[k] = v
        }
        return map
    }

    private fun install(isServer: Boolean) {
        val installable: Installable
        try {
            val selectedLoaderVersion = selectedLoaderVersionFlow.value
            val selectedMinecraftVersion = selectedMinecraftVersionFlow.value
            val description = descriptionFlow.value
            val name = nameFlow.value
            installable = VanillaInstallable(
                MinecraftManager.getMinecraftVersion(selectedMinecraftVersion),
                selectedLoaderVersion, description
            )
            installable.instanceName = name
            installable.isReinstall = false
            installable.isServer = isServer
            val success = installable.startInstall()
            if (success) {
                //loaderTypeNoneRadioButton.setSelected(true);
                //selectedLoaderTypeChanged(null);

                //minecraftVersionTable.setRowSelectionInterval(0, 0);
            }
        } catch (e: InvalidMinecraftVersion) {
            LOG.error("error", e)
        }
    }

    override fun addOnFontChanged(consumer: Consumer<Font>) {
        scope.launch {
            fontFlow.collect { t: Font -> consumer.accept(t) }
        }
    }

    override fun setName(name: String) {
        nameDirty = true
        nameFlow.value = (name)
    }

    override fun addOnNameChanged(name: Consumer<String?>) {
        scope.launch {
            nameFlow.collect { t: String? -> name.accept(t) }
        }
    }

    override fun setDescription(description: String) {
        descriptionDirty = true
        descriptionFlow.value = (description)
    }

    override fun addOnDescriptionChanged(name: Consumer<String?>) {
        scope.launch {
            descriptionFlow.collect { t -> name.accept(t) }
        }
    }

    override fun showReleaseOption(): Boolean {
        return ConfigManager.getConfigItem("minecraft.release.enabled", true)
    }

    override fun showExperimentOption(): Boolean {
        return ConfigManager.getConfigItem("minecraft.experiment.enabled", true)
    }

    override fun showSnapshotOption(): Boolean {
        return ConfigManager.getConfigItem("minecraft.snapshot.enabled", true)
    }

    override fun showOldAlphaOption(): Boolean {
        return ConfigManager.getConfigItem("minecraft.old_alpha.enabled", true)
    }

    override fun showOldBetaOption(): Boolean {
        return ConfigManager.getConfigItem("minecraft.old_beta.enabled", true)
    }

    override fun setReleaseSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.RELEASE] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override fun addOnReleaseEnabledChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            minecraftVersionTypeFiltersFlow.collect { it: Map<VersionManifestVersionType, Boolean> ->
                onChanged.accept(
                    it[VersionManifestVersionType.RELEASE] ?: false
                )
            }
        }
    }

    override fun setExperimentSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.EXPERIMENT] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override fun addOnExperimentEnabledChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            minecraftVersionTypeFiltersFlow.collect { it: Map<VersionManifestVersionType, Boolean> ->
                onChanged.accept(
                    it[VersionManifestVersionType.EXPERIMENT] ?: false
                )
            }
        }
    }

    override fun setSnapshotSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.SNAPSHOT] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override fun addOnSnapshotEnabledChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            minecraftVersionTypeFiltersFlow.collect { it: Map<VersionManifestVersionType, Boolean> ->
                onChanged.accept(
                    it[VersionManifestVersionType.SNAPSHOT] ?: false
                )
            }
        }
    }

    override fun setOldAlphaSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.OLD_ALPHA] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override fun addOnOldAlphaEnabledChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            minecraftVersionTypeFiltersFlow.collect { it: Map<VersionManifestVersionType, Boolean> ->
                onChanged.accept(
                    it[VersionManifestVersionType.OLD_ALPHA] ?: false
                )
            }
        }
    }

    override fun setOldBetaSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.OLD_BETA] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override fun addOnOldBetaEnabledChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            minecraftVersionTypeFiltersFlow.collect { it: Map<VersionManifestVersionType, Boolean> ->
                onChanged.accept(
                    it[VersionManifestVersionType.OLD_BETA] ?: false
                )
            }
        }
    }

    override fun setMinecraftVersion(newVersion: String?) {
        selectedMinecraftVersionFlow.value = newVersion
    }

    override fun addOnMinecraftVersionsChanged(onChanged: Consumer<Array<MCVersionRow>>) {
        scope.launch {
            versionsFlow.collect { t: Array<MCVersionRow> ->
                onChanged.accept(t)
            }
        }
    }

    override fun showFabricOption(): Boolean {
        return ConfigManager.getConfigItem("loaders.fabric.enabled", true)
    }

    override fun addOnFabricVisibleChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            fabricVisibleFlow.collect { t: Boolean -> onChanged.accept(t) }
        }
    }

    override fun showForgeOption(): Boolean {
        return ConfigManager.getConfigItem("loaders.forge.enabled", true)
    }

    override fun addOnForgeVisibleChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            forgeVisibleFlow.collect { t: Boolean -> onChanged.accept(t) }
        }
    }

    override fun showQuiltOption(): Boolean {
        return ConfigManager.getConfigItem("loaders.quilt.enabled", false)
    }

    override fun addOnQuiltVisibleChanged(onChanged: Consumer<Boolean>) {
        scope.launch {
            quiltVisibleFlow.collect { t: Boolean -> onChanged.accept(t) }
        }
    }

    override fun setLoaderType(loader: LoaderType?) {
        selectedLoaderTypeFlow.value = loader
    }

    override fun setLoaderVersion(loaderVersion: String) {
        scope.launch {
            loaderVersions.first()
                .let { versions ->
                    if (versions != null) {
                        for (version in versions) {
                            if (version.version == loaderVersion) {
                                selectedLoaderVersionFlow.value = version
                                break
                            }
                        }
                    }
                }
        }
    }

    override fun addOnLoaderVersionsChanged(consumer: Consumer<Array<String>?>) {
        scope.launch {
            loaderVersions.map { versions: Array<LoaderVersion>? ->
                versions?.map { it.version }?.toTypedArray()
            }.collect {
                consumer.accept(it)
            }
        }
    }

    override fun addOnLoaderLoadingListener(consumer: Consumer<Boolean>) {
        scope.launch {
            loaderVersionsLoadingFlow.collect { t: Boolean -> consumer.accept(t) }
        }
    }

    override fun addOnLoaderGroupEnabledListener(consumer: Consumer<Boolean>) {
        scope.launch {
            loaderGroupEnabledFlow.collect { t: Boolean -> consumer.accept(t) }
        }
    }

    override fun getFabricDisabledMCVersions(): List<String> {
        return ConfigManager.getConfigItem("loaders.fabric.disabledMinecraftVersions", ArrayList())
    }

    override fun getForgeDisabledMCVersions(): List<String> {
        return ConfigManager.getConfigItem("loaders.forge.disabledMinecraftVersions", ArrayList())
    }

    override fun getQuiltDisabledMCVersions(): List<String> {
        return ConfigManager.getConfigItem("loaders.quilt.disabledMinecraftVersions", ArrayList())
    }

    override fun addOnCreateServerVisibleChanged(consumer: Consumer<Boolean>) {
        scope.launch {
            createServerVisibleObservable.collect { t: Boolean -> consumer.accept(t) }
        }
    }

    override fun createServer() {
        install(true)
    }

    override fun createInstance() {
        install(false)
    }

    override fun onSettingsSaved() {
        fontFlow.value = (App.THEME.boldFont)
    }

    companion object {
        private val LOG = LogManager.getLogger(
            VanillaPacksTab::class.java
        )
    }
}