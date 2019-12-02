package com.shananton.metronome.test

import com.h0tk3y.player.*
import com.h0tk3y.player.test.TestableMusicApp
import com.shananton.metronome.plugin.MetronomePlugin
import org.junit.*
import org.junit.Test
import java.io.File
import kotlin.test.*

const val metronomePluginClassName = "com.shananton.metronome.plugin.MetronomePlugin"

class PluginSupportTest {

    private val defaultEnabledPlugins = setOf(
        ConsoleControlsPlugin::class.java.canonicalName,
        StaticPlaylistsLibraryContributor::class.java.canonicalName,
        MetronomePlugin::class.java.canonicalName
    )

    private fun withApp(
        wipePersistedData: Boolean = false,
        pluginClasspath: List<File> = emptyList(),
        enabledPlugins: Set<String> = defaultEnabledPlugins,
        doTest: TestableMusicApp.() -> Unit
    ) {
        val app = TestableMusicApp(pluginClasspath, enabledPlugins)
        if (wipePersistedData) {
            app.wipePersistedPluginData()
        }
        app.use {
            it.init()
            it.doTest()
        }
    }

    @Test
    fun testMetronomePluginArithmetic() {
        withApp {
            val plugin = assertNotNull(
                findSinglePlugin(metronomePluginClassName)
                        as? MetronomePlugin
            )
            plugin.bpm = 10.0
            assertEquals(plugin.globalEnabled, true)
            plugin.bpm = 0.0
            assertEquals(plugin.globalEnabled, false)
            plugin.bpm = 60.0
            assertEquals(plugin.intervalMs, 1000.0)
            plugin.intervalMs = 250.0
            assertEquals(plugin.bpm, 240.0)
        }
    }
}
