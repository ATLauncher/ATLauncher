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

import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.exception.ApolloException
import com.atlauncher.App
import com.atlauncher.data.installables.Installable
import com.atlauncher.data.installables.VanillaInstallable
import com.atlauncher.data.minecraft.VersionManifestVersion
import com.atlauncher.data.minecraft.VersionManifestVersionType
import com.atlauncher.data.minecraft.loaders.LoaderType
import com.atlauncher.data.minecraft.loaders.LoaderVersion
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader
import com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader
import com.atlauncher.evnt.listener.SettingsListener
import com.atlauncher.evnt.manager.SettingsManager
import com.atlauncher.exceptions.InvalidMinecraftVersion
import com.atlauncher.graphql.GetLoaderVersionsForMinecraftVersionQuery
import com.atlauncher.gui.tabs.VanillaPacksTab
import com.atlauncher.managers.ConfigManager
import com.atlauncher.managers.InstanceManager
import com.atlauncher.managers.LogManager
import com.atlauncher.managers.MinecraftManager
import com.atlauncher.network.GraphqlClient
import com.atlauncher.utils.Pair
import com.atlauncher.utils.Utils
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel
import com.atlauncher.viewmodel.base.IVanillaPacksViewModel.MCVersionRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.mini2Dx.gettext.GetText
import java.awt.Font
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors


/**
 * 25 / 06 / 2022
 */
class VanillaPacksViewModel : IVanillaPacksViewModel, SettingsListener {
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Filters applied to the version table
     */
    private val minecraftVersionTypeFiltersFlow = MutableStateFlow(mapOf(VersionManifestVersionType.RELEASE to true))

    /**
     * Name is dirty, as the user has inputted something
     */
    private var nameDirty = false

    /**
     * Name to display
     */
    override val name = MutableStateFlow<String?>(null)

    /**
     * Description is dirty, as the user has inputted something
     */
    private var descriptionDirty = false

    /**
     * Description to display
     */
    override val description = MutableStateFlow<String?>(null)

    private val selectedMinecraftVersionFlow = MutableStateFlow<String?>(null)
    override val selectedLoaderType = MutableStateFlow<LoaderType?>(null)
    private val selectedLoaderVersionFlow = MutableStateFlow<LoaderVersion?>(null)

    override val loaderLoading = MutableStateFlow(false)

    override val font = MutableStateFlow<Font>(App.THEME.boldFont)

    // Consumers
    /**
     * Represents the version list
     */
    override val minecraftVersions: Flow<Array<MCVersionRow>> by lazy {
        minecraftVersionTypeFiltersFlow.map { versionFilter: Map<VersionManifestVersionType, Boolean> ->
            val filtered = versionFilter.filter { it.value }.map { it.key }.toList()
            val fmt = DateTimeFormat.forPattern(App.settings.dateFormat)

            MinecraftManager.getFilteredMinecraftVersions(filtered).map { it: VersionManifestVersion ->
                MCVersionRow(
                    it.id, fmt.print(ISODateTimeFormat.dateTimeParser().parseDateTime(it.releaseTime)), it.id
                )
            }.toTypedArray()
        }
    }

    override val loaderVersionsDropDownEnabled = MutableStateFlow(false)

    /**
     * Presents the loader versions for the user to select
     */
    override val loaderVersions = MutableStateFlow<Array<LoaderVersion>?>(null)

    override val createServerEnabled = MutableStateFlow(false)
    override val createInstanceEnabled = MutableStateFlow(false)

    override val isFabricVisible by lazy {
        selectedMinecraftVersionFlow.map { version -> !fabricDisabledMCVersions.contains(version) }
    }

    override val isLegacyFabricVisible: Flow<Boolean> by lazy {
        selectedMinecraftVersionFlow.map { version -> !legacyFabricDisabledMCVersions.contains(version) }
    }

    override val isForgeVisible by lazy {
        selectedMinecraftVersionFlow.map { version -> !forgeDisabledMCVersions.contains(version) }
    }

    override val isQuiltVisible by lazy {
        selectedMinecraftVersionFlow.map { version -> !quiltDisabledMCVersions.contains(version) }
    }


    override val loaderTypeFabricSelected: Flow<Boolean> = selectedLoaderType.map { it == LoaderType.FABRIC }
    override val loaderTypeFabricEnabled = MutableStateFlow(true)

    override val loaderTypeForgeSelected: Flow<Boolean> = selectedLoaderType.map { it == LoaderType.FORGE }
    override val loaderTypeLegacyFabricSelected: Flow<Boolean> =
        selectedLoaderType.map { it == LoaderType.LEGACY_FABRIC }
    override val loaderTypeForgeEnabled = MutableStateFlow(true)
    override val loaderTypeLegacyFabricEnabled = MutableStateFlow(true)

    override val loaderTypeNoneSelected: Flow<Boolean> = selectedLoaderType.map { it == null }
    override val loaderTypeNoneEnabled = MutableStateFlow(true)

    override val loaderTypeQuiltSelected: Flow<Boolean> = selectedLoaderType.map { it == LoaderType.QUILT }
    override val loaderTypeQuiltEnabled = MutableStateFlow(true)

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
            val selectedMinecraftVersion = selectedMinecraftVersionFlow.value ?: return
            val description = description.value
            val name = name.value

            installable = VanillaInstallable(
                MinecraftManager.getMinecraftVersion(selectedMinecraftVersion), selectedLoaderVersion, description
            )
            installable.instanceName = name
            installable.isReinstall = false
            installable.isServer = isServer
            val success = installable.startInstall()
            if (success) {
                // TODO Reset server
                //nameFieldDirty = false
                //descriptionFieldDirty = false
                //loaderTypeNoneRadioButton.isSelected = true
                //selectedLoaderTypeChanged(null)
                //minecraftVersionTable!!.setRowSelectionInterval(0, 0)
            }
        } catch (e: InvalidMinecraftVersion) {
            LogManager.logStackTrace(e)
        }
    }

    private fun isNameDirty() = !(name.value == String.format(
        "Minecraft %s", selectedMinecraftVersionFlow.value
    ) || selectedLoaderType.value != null && name.value == String.format(
        "Minecraft %s with %s", selectedMinecraftVersionFlow.value, selectedLoaderType.value
    ))

    override fun setName(name: String) {
        this.name.value = (name)
        nameDirty = isNameDirty()
    }

    private fun isDescriptionDirty() = !(description.value == String.format(
        "Minecraft %s", selectedMinecraftVersionFlow.value
    ) || selectedLoaderType.value != null && description.value == String.format(
        "Minecraft %s with %s", selectedMinecraftVersionFlow.value, selectedLoaderType.value
    ))

    override fun setDescription(description: String) {
        descriptionDirty = isDescriptionDirty()
        this.description.value = (description)
    }

    override val showReleaseOption: Boolean by lazy { ConfigManager.getConfigItem("minecraft.release.enabled", true) }

    override val showExperimentOption: Boolean by lazy {
        ConfigManager.getConfigItem(
            "minecraft.experiment.enabled",
            true
        )
    }

    override val showSnapshotOption: Boolean by lazy { ConfigManager.getConfigItem("minecraft.snapshot.enabled", true) }

    override val showOldAlphaOption: Boolean by lazy {
        ConfigManager.getConfigItem(
            "minecraft.old_alpha.enabled",
            true
        )
    }

    override val showOldBetaOption: Boolean by lazy { ConfigManager.getConfigItem("minecraft.old_beta.enabled", true) }

    override fun setReleaseSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.RELEASE] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override val releaseSelected: Flow<Boolean> = minecraftVersionTypeFiltersFlow.map {
        it[VersionManifestVersionType.RELEASE] ?: false
    }

    override val releaseEnabled: Flow<Boolean> = releaseSelected.combine(minecraftVersionTypeFiltersFlow) { a, b ->
        a && (b.size == 1)
    }

    override fun setExperimentSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.EXPERIMENT] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override val experimentSelected: Flow<Boolean> = minecraftVersionTypeFiltersFlow.map {
        it[VersionManifestVersionType.EXPERIMENT] ?: false
    }

    override val experimentEnabled: Flow<Boolean> =
        experimentSelected.combine(minecraftVersionTypeFiltersFlow) { a, b ->
            a && (b.size == 1)
        }

    override fun setSnapshotSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.SNAPSHOT] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override val snapshotSelected: Flow<Boolean> = minecraftVersionTypeFiltersFlow.map {
        it[VersionManifestVersionType.SNAPSHOT] ?: false
    }

    override val snapshotEnabled: Flow<Boolean> = snapshotSelected.combine(minecraftVersionTypeFiltersFlow) { a, b ->
        a && (b.size == 1)
    }

    override fun setOldAlphaSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.OLD_ALPHA] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override val oldAlphaSelected: Flow<Boolean> = minecraftVersionTypeFiltersFlow.map {
        it[VersionManifestVersionType.OLD_ALPHA] ?: false
    }

    override val oldAlphaEnabled: Flow<Boolean> = oldAlphaSelected.combine(minecraftVersionTypeFiltersFlow) { a, b ->
        a && (b.size == 1)
    }

    override fun setOldBetaSelected(b: Boolean) {
        val map = HashMap(minecraftVersionTypeFiltersFlow.value)
        map[VersionManifestVersionType.OLD_BETA] = b
        minecraftVersionTypeFiltersFlow.value = map.copy()
    }

    override val oldBetaSelected: Flow<Boolean> = minecraftVersionTypeFiltersFlow.map {
        it[VersionManifestVersionType.OLD_BETA] ?: false
    }

    override val oldBetaEnabled: Flow<Boolean> = oldBetaSelected.combine(minecraftVersionTypeFiltersFlow) { a, b ->
        a && (b.size == 1)
    }

    override fun setSelectedMinecraftVersion(newVersion: String?) {
        selectedMinecraftVersionFlow.value = newVersion
    }

    override val showFabricOption: Boolean by lazy { ConfigManager.getConfigItem("loaders.fabric.enabled", true) }

    override val showForgeOption: Boolean by lazy { ConfigManager.getConfigItem("loaders.forge.enabled", true) }
    override val showLegacyFabricOption: Boolean by lazy {
        ConfigManager.getConfigItem(
            "loader.legacyfabric.enabled",
            true
        )
    }

    override val showQuiltOption: Boolean by lazy { ConfigManager.getConfigItem("loaders.quilt.enabled", false) }

    override fun setLoaderType(loader: LoaderType?) {
        selectedLoaderType.value = loader
    }

    override fun setLoaderVersion(loaderVersion: String) {
        scope.launch {
            loaderVersions.first().let { versions ->
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

    private val fabricDisabledMCVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.fabric.disabledMinecraftVersions", emptyList()
        )
    }

    private val legacyFabricDisabledMCVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.fabric.disabledMinecraftVersions", emptyList()
        )
    }

    private val forgeDisabledMCVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.forge.disabledMinecraftVersions", emptyList()
        )
    }

    private val quiltDisabledMCVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.quilt.disabledMinecraftVersions", emptyList()
        )
    }

    override fun createServer() {
        install(true)
    }

    override fun createInstance() {
        install(false)
    }

    override fun onSettingsSaved() {
        font.value = (App.THEME.boldFont)
    }

    override val warnUserAboutServer: Boolean
        get() = InstanceManager.getInstances().size == 0

    init {
        SettingsManager.addListener(this)
        scope.launch {
            selectedMinecraftVersionFlow.collect { selectedVersion ->
                if (selectedVersion != null) {
                    try {
                        val version = MinecraftManager.getMinecraftVersion(selectedVersion)
                        createServerEnabled.value = version.hasServer()
                    } catch (ignored: InvalidMinecraftVersion) {
                        createServerEnabled.value = false
                    }
                } else {
                    createServerEnabled.value = false
                }

                val defaultNameFieldValue = String.format(
                    "Minecraft %s", selectedVersion
                )
                if (name.value == null || name.value!!.isEmpty() || !nameDirty) {
                    nameDirty = false
                    name.value = (defaultNameFieldValue)
                }
                if (description.value == null || description.value!!.isEmpty() || !descriptionDirty) {
                    descriptionDirty = false
                    description.value = (defaultNameFieldValue)
                }
            }
        }

        scope.launch {
            selectedLoaderType.combine(selectedMinecraftVersionFlow) { a, b ->
                a to b
            }.collect { (selectedLoader, selectedMinecraftVersion) ->
                if (selectedMinecraftVersion == null) return@collect
                loaderVersionsDropDownEnabled.value = false
                if (selectedLoader == null) {
                    // update the name and description fields if they're not dirty
                    val defaultNameFieldValue = String.format("Minecraft %s", selectedMinecraftVersion)
                    if (!nameDirty) {
                        name.value = defaultNameFieldValue
                    }
                    if (!descriptionDirty) {
                        description.value = defaultNameFieldValue
                    }
                    loaderVersions.value = arrayOf(LoaderVersion(GetText.tr("Select Loader First")))
                    return@collect
                }
                loaderLoading.value = true

                setLoaderGroupEnabled(false)

                // Legacy Forge doesn't support servers easily
                val enableCreateServers = (selectedLoader !== LoaderType.FORGE || !Utils.matchVersion(
                    selectedMinecraftVersion, "1.5", true, true
                ))
                if (ConfigManager.getConfigItem("useGraphql.vanillaLoaderVersions", false) == true) {
                    apolloLoad(selectedLoader, selectedMinecraftVersion, enableCreateServers)
                } else {
                    legacyLoad(selectedLoader, selectedMinecraftVersion, enableCreateServers)
                }
            }
        }
    }

    suspend fun apolloLoad(
        selectedLoader: LoaderType, selectedMinecraftVersion: String?, enableCreateServers: Boolean
    ) {
        try {
            val response = GraphqlClient.apolloClient.query(
                GetLoaderVersionsForMinecraftVersionQuery(
                    selectedMinecraftVersion!!
                )
            ).toBuilder().httpCachePolicy(
                HttpCachePolicy.Policy(
                    HttpCachePolicy.FetchStrategy.CACHE_FIRST, 5, TimeUnit.MINUTES, false
                )
            ).build().await()

            val loaderVersionsList: MutableList<LoaderVersion> = ArrayList()
            when (selectedLoader) {
                LoaderType.FABRIC -> {
                    loaderVersionsList.addAll(response.data!!.loaderVersions().fabric().stream()
                        .filter { fv: GetLoaderVersionsForMinecraftVersionQuery.Fabric ->
                            !disabledFabricVersions.contains(fv.version())
                        }.map { version: GetLoaderVersionsForMinecraftVersionQuery.Fabric ->
                            LoaderVersion(version.version(), false, "Fabric")
                        }.collect(Collectors.toList())
                    )
                }

                LoaderType.FORGE -> {
                    loaderVersionsList.addAll(response.data!!.loaderVersions().forge().stream()
                        .filter { fv: GetLoaderVersionsForMinecraftVersionQuery.Forge ->
                            !disabledForgeVersions.contains(fv.version())
                        }.map { version: GetLoaderVersionsForMinecraftVersionQuery.Forge ->
                            val lv = LoaderVersion(
                                version.version(), version.rawVersion(), version.recommended(), "Forge"
                            )
                            if (version.installerSha1Hash() != null && version.installerSize() != null) {
                                lv.downloadables["installer"] = Pair(
                                    version.installerSha1Hash(), version.installerSize()!!.toLong()
                                )
                            }
                            if (version.universalSha1Hash() != null && version.universalSize() != null) {
                                lv.downloadables["universal"] = Pair(
                                    version.universalSha1Hash(), version.universalSize()!!.toLong()
                                )
                            }
                            if (version.clientSha1Hash() != null && version.clientSize() != null) {
                                lv.downloadables["client"] = Pair(
                                    version.clientSha1Hash(), version.clientSize()!!.toLong()
                                )
                            }
                            if (version.serverSha1Hash() != null && version.serverSize() != null) {
                                lv.downloadables["server"] = Pair(
                                    version.serverSha1Hash(), version.serverSize()!!.toLong()
                                )
                            }
                            lv
                        }.collect(Collectors.toList())
                    )
                }

                LoaderType.QUILT -> {
                    loaderVersionsList.addAll(response.data!!.loaderVersions().quilt().stream()
                        .filter { fv: GetLoaderVersionsForMinecraftVersionQuery.Quilt ->
                            !disabledQuiltVersions.contains(fv.version())
                        }.map { version: GetLoaderVersionsForMinecraftVersionQuery.Quilt ->
                            LoaderVersion(version.version(), false, "Quilt")
                        }.collect(Collectors.toList())
                    )
                }

                LoaderType.LEGACY_FABRIC -> {

                    loaderVersionsList.addAll(response.data!!.loaderVersions().legacyfabric()
                        .stream()
                        .filter { fv -> !disabledLegacyFabricVersions.contains(fv.version()) }
                        .map { version ->
                            LoaderVersion(
                                version.version(),
                                false,
                                "LegacyFabric"
                            )
                        }
                        .collect(Collectors.toList())
                    )
                }
            }
            if (loaderVersionsList.size == 0) {
                setLoaderGroupEnabled(true, enableCreateServers)
                loaderVersions.value = arrayOf(LoaderVersion(GetText.tr("No Versions Found")))
                return
            }
            loaderVersions.value = loaderVersionsList.toTypedArray()

            setLoaderGroupEnabled(true, enableCreateServers)

            updateNameAndDescription(selectedMinecraftVersion, selectedLoader)
        } catch (e: ApolloException) {
            LogManager.logStackTrace("Error fetching loading versions", e)
            setLoaderGroupEnabled(true, enableCreateServers)
            loaderVersions.value = arrayOf(LoaderVersion(GetText.tr("Error Getting Versions")))
        }
    }

    private fun setLoaderGroupEnabled(enabled: Boolean, enableCreateServers: Boolean = enabled) {
        loaderTypeNoneEnabled.value = enabled
        loaderTypeFabricEnabled.value = enabled
        loaderTypeForgeEnabled.value = enabled
        loaderTypeLegacyFabricEnabled.value = enabled
        loaderTypeQuiltEnabled.value = enabled
        createServerEnabled.value = enableCreateServers
        createInstanceEnabled.value = enabled
        loaderVersionsDropDownEnabled.value = enabled
    }

    private val disabledQuiltVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.quilt.disabledVersions", emptyList()
        )
    }

    private val disabledFabricVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.fabric.disabledVersions", emptyList()
        )
    }

    private val disabledLegacyFabricVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.legacyfabric.disabledVersions", emptyList()
        )
    }

    private val disabledForgeVersions: List<String> by lazy {
        ConfigManager.getConfigItem(
            "loaders.forge.disabledVersions", emptyList()
        )
    }

    /**
     * Use legacy loading mechanic
     */
    suspend fun legacyLoad(
        selectedLoader: LoaderType, selectedMinecraftVersion: String, enableCreateServers: Boolean
    ) {
        val loaderVersionsList: MutableList<LoaderVersion> = ArrayList()
        loaderVersionsList.addAll(
            when (selectedLoader) {
                LoaderType.FABRIC -> FabricLoader.getChoosableVersions(selectedMinecraftVersion)
                LoaderType.FORGE -> ForgeLoader.getChoosableVersions(selectedMinecraftVersion)
                LoaderType.QUILT -> QuiltLoader.getChoosableVersions(selectedMinecraftVersion)
                LoaderType.LEGACY_FABRIC -> LegacyFabricLoader.getChoosableVersions(selectedMinecraftVersion)
            }
        )
        if (loaderVersionsList.size == 0) {
            setLoaderGroupEnabled(true, enableCreateServers)
            loaderVersions.value = arrayOf(LoaderVersion(GetText.tr("No Versions Found")))
            return
        }

        loaderVersions.value = loaderVersionsList.toTypedArray()

        setLoaderGroupEnabled(true, enableCreateServers)

        updateNameAndDescription(selectedMinecraftVersion, selectedLoader)
    }

    /**
     * Update the name and description fields if they're not dirty with loader type information
     */
    private fun updateNameAndDescription(
        selectedMinecraftVersion: String, selectedLoader: LoaderType
    ) {
        val defaultNameFieldValue = String.format(
            "Minecraft %s with %s", selectedMinecraftVersion, selectedLoader.toString()
        )
        if (!nameDirty) {
            name.value = defaultNameFieldValue
        }
        if (!descriptionDirty) {
            description.value = defaultNameFieldValue
        }
    }

    companion object {
        private val LOG = org.apache.logging.log4j.LogManager.getLogger(
            VanillaPacksTab::class.java
        )
    }
}