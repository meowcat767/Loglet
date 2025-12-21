package site.meowcat.loglet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import site.meowcat.loglet.data.Trip

class TripTest {

    @Test
    fun testTripInitialization() {
        val name = "Road Trip"
        val startTime = System.currentTimeMillis()
        val trip = Trip(name, startTime)

        assertEquals(name, trip.name)
        assertEquals(startTime, trip.startTime)
        assertEquals(0L, trip.endTime)
    }

    @Test
    fun testTripEndTimeUpdate() {
        val trip = Trip("Walk", 1000L)
        val endTime = 2000L
        trip.endTime = endTime

        assertEquals(endTime, trip.endTime)
        assertTrue(trip.endTime > trip.startTime)
    }
}
