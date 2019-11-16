package com.h0tk3y.player

import java.io.File

open class MusicApp(
    private val pluginClasspath: List<File>,
    private val enabledPluginClasses: Set<String>
) : AutoCloseable {
    fun init() {
        /**
         * TODO: Инициализировать плагины с помощью функции [MusicPlugin.init],
         *       предоставив им байтовые потоки их состояния (для тех плагинов, для которых они сохранены).
         *       Обратите внимание на cлучаи, когда необходимо выбрасывать исключения
         *       [IllegalPluginException] и [PluginClassNotFoundException].
         **/

        musicLibrary // access to initialize
        player.init()
    }

    override fun close() {
        if (isClosed) return
        isClosed = true

        /** TODO: Сохранить состояние плагинов с помощью [MusicPlugin.persist]. */
    }

    fun wipePersistedPluginData() {
        // TODO: Удалить сохранённое состояние плагинов.
    }

    private val pluginClassLoader: ClassLoader = TODO("Создать загрузчик классов для плагинов.")

    private val plugins: List<MusicPlugin> by lazy {
        /**
         * TODO используя [pluginClassLoader] и следуя контракту [MusicPlugin],
         *      загрузить плагины, перечисленные в [enabledPluginClasses].
         *      Эта функция не должна вызывать [MusicPlugin.init]
         */
        emptyList<MusicPlugin>()
    }

    fun findSinglePlugin(pluginClassName: String): MusicPlugin? =
        TODO("Если есть единственный плагин, принадлежащий типу по имени pluginClassName, вернуть его, иначе null.")

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
            ), isResumed = false)
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