package com.h0tk3y.player

import java.io.File
import java.net.URLClassLoader
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

open class MusicApp(
    private val pluginClasspath: List<File>,
    private val enabledPluginClasses: Set<String>
) : AutoCloseable {
    companion object {
        const val PERSISTED_STATE_SUFFIX = ".persist"
        private fun persistFilename(plugin: MusicPlugin) = plugin.pluginId + PERSISTED_STATE_SUFFIX
    }

    fun init() {
        plugins.forEach { plugin ->
            val file = File(persistFilename(plugin))
            if (file.exists()) {
                file.inputStream().use {
                    plugin.init(it)
                }
            } else {
                plugin.init(null)
            }
        }
        musicLibrary // access to initialize
        player.init()
    }

    override fun close() {
        if (isClosed) return
        isClosed = true
        wipePersistedPluginData()
        plugins.forEach { plugin ->
            File(persistFilename(plugin)).outputStream().use {
                plugin.persist(it)
            }
        }
    }

    fun wipePersistedPluginData() {
        plugins.forEach { File(persistFilename(it)).delete() }
    }

    private val pluginClassLoader: ClassLoader = URLClassLoader(
        pluginClasspath.map { it.toURI().toURL() }.toTypedArray()
    )

    private val plugins: List<MusicPlugin> by lazy {
        enabledPluginClasses.toList().map { pluginName ->
            val pluginKClass = try {
                pluginClassLoader.loadClass(pluginName).kotlin
            } catch (e: ClassNotFoundException) {
                throw PluginClassNotFoundException(pluginName)
            }
            pluginKClass.primaryConstructor?.let { constructor ->
                if (constructor.parameters.size == 1 && constructor.parameters[0].type == MusicApp::class.createType()) {
                    (constructor.call(this) as? MusicPlugin)?.let {
                        return@map it
                    }
                }
            }
            pluginKClass.constructors.find { it.parameters.isEmpty() }?.let { constructor ->
                (pluginKClass.memberProperties.find { it.name == "musicAppInstance" } as? KMutableProperty<*>)
                    ?.let { property ->
                        if (property.returnType == MusicApp::class.createType()) {
                            (constructor.call().also {
                                property.setter.call(it, this)
                            } as? MusicPlugin)?.let {
                                return@map it
                            }
                        }
                    }
            }
            throw IllegalPluginException(pluginKClass.java)
        }
    }

    fun findSinglePlugin(pluginClassName: String): MusicPlugin? =
        plugins.singleOrNull {it::class.qualifiedName == pluginClassName}

    fun <T : MusicPlugin> getPlugins(pluginClass: Class<T>): List<T> =
        plugins.filterIsInstance(pluginClass)

    private val musicLibraryContributors: List<MusicLibraryContributorPlugin>
        get() = getPlugins(MusicLibraryContributorPlugin::class.java)

    protected val playbackListeners: List<PlaybackListenerPlugin>
        get() = getPlugins(PlaybackListenerPlugin::class.java)

    val musicLibrary: MusicLibrary by lazy {
        musicLibraryContributors
            .sortedWith(compareBy({ it.preferredOrder }, { it.pluginId }))
            .fold(MusicLibrary(mutableListOf())) { acc, it -> it.contribute(acc) }
    }

    open val player: MusicPlayer by lazy {
        JLayerMusicPlayer(
            playbackListeners
        )
    }

    fun startPlayback(playlist: Playlist, fromPosition: Int) {
        player.playbackState = PlaybackState.Playing(
            PlaylistPosition(
                playlist,
                fromPosition
            ), isResumed = false
        )
    }

    fun nextOrStop() = player.playbackState.playlistPosition?.let {
        val nextPosition = it.position + 1
        player.playbackState =
            if (nextPosition in it.playlist.tracks.indices)
                PlaybackState.Playing(
                    PlaylistPosition(
                        it.playlist,
                        nextPosition
                    ), isResumed = false
                )
            else
                PlaybackState.Stopped
    }

    @Volatile
    var isClosed = false
        private set
}