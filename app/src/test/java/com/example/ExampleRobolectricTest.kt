package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ColorGradeSettings
import com.example.model.LutLibrary
import com.example.util.ColorGradingEngine
import com.example.viewmodel.VideoEditorViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PrismCut", appName)
    }

    @Test
    fun colorGradingEngine_createsValidColorMatrix() {
        val tealOrange = LutLibrary.presets.find { it.id == "teal_orange" }!!.settings
        val matrix = ColorGradingEngine.createColorMatrix(tealOrange)
        assertNotNull(matrix)
        assertEquals(20, matrix.values.size)

        // Standard neutral Rec.709
        val neutral = ColorGradeSettings()
        val neutralMatrix = ColorGradingEngine.createColorMatrix(neutral)
        assertNotNull(neutralMatrix)
    }

    @Test
    fun videoEditorViewModel_splitAndPlaybackOperations() {
        val viewModel = VideoEditorViewModel()
        val initialTracks = viewModel.uiState.value.project.tracks
        assertTrue(initialTracks.isNotEmpty())

        // Seek
        viewModel.seekTo(2500L)
        assertEquals(2500L, viewModel.uiState.value.currentPositionMs)

        // Split clip at playhead
        viewModel.splitClipAtPlayhead()
        val updatedTracks = viewModel.uiState.value.project.tracks
        assertTrue(updatedTracks.isNotEmpty())

        // Apply LUT
        val cyberpunk = LutLibrary.presets.find { it.id == "cyberpunk" }!!
        viewModel.applyLut(cyberpunk)
        assertEquals("cyberpunk", viewModel.activeColorGrade.value.activeLutId)
    }
}
