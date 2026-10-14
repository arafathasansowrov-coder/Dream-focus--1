package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.DesignatedStudyApp
import com.example.data.model.DistractionAlertEvent
import com.example.service.DistractionShieldService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DistractionShieldTest {

    @Test
    fun testDesignatedStudyAppCreationAndState() {
        val app = DesignatedStudyApp(
            packageName = "com.google.android.apps.docs",
            appName = "Google Docs",
            category = "Notes & Docs",
            isEnabled = true
        )

        assertEquals("com.google.android.apps.docs", app.packageName)
        assertEquals("Google Docs", app.appName)
        assertTrue(app.isEnabled)
    }

    @Test
    fun testDistractionAlertEventProperties() {
        val event = DistractionAlertEvent(
            appName = "YouTube",
            packageName = "com.google.android.youtube",
            reason = "Navigated away from designated study apps"
        )

        assertEquals("YouTube", event.appName)
        assertEquals("com.google.android.youtube", event.packageName)
        assertTrue(event.reason.contains("Navigated away"))
        assertNotNull(event.id)
    }

    @Test
    fun testDistractionShieldServiceCompanionState() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertNotNull(context)

        // Dismiss alert test
        DistractionShieldService.dismissLastAlert()
        assertNull(DistractionShieldService.lastDetectedDistraction.value)

        // Simulate alert event
        DistractionShieldService.simulateDistractionAlert(
            context = context,
            appName = "TikTok",
            packageName = "com.zhiliaoapp.musically"
        )

        val lastAlert = DistractionShieldService.lastDetectedDistraction.value
        assertNotNull(lastAlert)
        assertEquals("TikTok", lastAlert?.appName)
        assertEquals("com.zhiliaoapp.musically", lastAlert?.packageName)

        // Clean up
        DistractionShieldService.dismissLastAlert()
        assertNull(DistractionShieldService.lastDetectedDistraction.value)
    }
}
