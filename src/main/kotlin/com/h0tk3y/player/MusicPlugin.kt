package com.h0tk3y.player

import java.io.InputStream
import java.io.OutputStream

interface MusicPlugin {
    val pluginId: String
        get() = javaClass.canonicalName

    /** Called upon application start to initialize the plugin. The [persistedState] is the byte stream written by
     * [persist], if present. */
    fun init(persistedState: InputStream?)

    /** Called on a plugin instance to instruct it to persist all of its state. The plugin is allowed to use the
     * [stateStream] for storing the state, but should not close the [stateStream].
     *
     * May be called multiple times during application execution.
     *
     * If [MusicApp.isClosed] is true on the [musicAppInstance], the plugin should also yield all of its resources
     * and gracefully teardown.*/
    fun persist(stateStream: OutputStream)

    /** A reference to the application instance.
     *
     * A plugin may override this property as the single parameter of the primary constructor or a mutable property
     * (then the class must contain a no-argument constructor).
     *
     * In both cases, the application that instantiates the plugin must provide the value for the property.
     * If this property cannot be initialized in either way, the application must throw an [IllegalPluginException]
     * */
    val musicAppInstance: MusicApp
}

class IllegalPluginException(val pluginClass: Class<*>) : Exception(
    "Illegal plugin class $pluginClass."
)

class PluginClassNotFoundException(val pluginClassName: String) : ClassNotFoundException(
    "Plugin class $pluginClassName not found."
)

interface PipelineContributorPlugin<T> : MusicPlugin {
    /** Plugins with lower preferred order should contribute to the pipeline earlier, that is, their results may
     * be altered by the plugins with higher preferred order. */
    val preferredOrder: Int

    fun contribute(current: T): T
}

interface MusicLibraryContributorPlugin : PipelineContributorPlugin<MusicLibrary>

interface PlaybackListenerPlugin : MusicPlugin {
    fun onPlaybackStateChange(oldPlaybackState: PlaybackState, newPlaybackState: PlaybackState)
}