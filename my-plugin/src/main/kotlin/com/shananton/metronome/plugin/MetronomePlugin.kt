package com.shananton.metronome.plugin

import com.h0tk3y.player.BpmReaderPlugin
import javazoom.jl.player.Player
import com.h0tk3y.player.MusicApp
import com.h0tk3y.player.PlaybackListenerPlugin
import com.h0tk3y.player.PlaybackState
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

class MetronomePlugin(override val musicAppInstance: MusicApp) : BpmReaderPlugin, PlaybackListenerPlugin {
    @Volatile
    private var enabled: Boolean = false
    @Volatile
    public var globalEnabled: Boolean = false

    companion object {
        private const val MS_IN_MINUTE = 60 * 1000
    }

    override var bpm: Double
        get() = MS_IN_MINUTE / intervalMs
        set(value) {
            if (value == 0.0) {
                globalEnabled = false
            } else {
                globalEnabled = true
                intervalMs = MS_IN_MINUTE / value
            }
        }
    @Volatile
    var intervalMs: Double = 1000.0
    private fun metronomeLoop() {
        while (true) {
            if (enabled && globalEnabled) {
                thread(isDaemon = true) {
                    File("sounds/metronome.mp3").inputStream().use {
                        Player(it).play()
                    }
                }
                Thread.sleep(intervalMs.toLong())
            }
        }
    }

    override fun init(persistedState: InputStream?) {
        thread(isDaemon = true, name = "metronomeLoop") { metronomeLoop() }
    }

    override fun onPlaybackStateChange(oldPlaybackState: PlaybackState, newPlaybackState: PlaybackState) {
        enabled = (newPlaybackState is PlaybackState.Playing)
    }

    override fun persist(stateStream: OutputStream) {

    }
}