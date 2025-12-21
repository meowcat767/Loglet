package site.meowcat.loglet

import org.junit.Assert.assertEquals
import org.junit.Test
import site.meowcat.loglet.data.ActivityLog

class ActivityLogTest {

    @Test
    fun testActivityLogCreation() {
        val label = "Walking"
        val startTime = System.currentTimeMillis()
        val log = ActivityLog(label, startTime)

        assertEquals(label, log.label)
        assertEquals(startTime, log.startTime)
    }

    @Test
    fun testActivityLogLocationFields() {
        val log = ActivityLog("Test", 1000L)
        log.latitude = 37.7749
        log.longitude = -122.4194

        assertEquals(37.7749, log.latitude, 0.0001)
        assertEquals(-122.4194, log.longitude, 0.0001)
    }
}
