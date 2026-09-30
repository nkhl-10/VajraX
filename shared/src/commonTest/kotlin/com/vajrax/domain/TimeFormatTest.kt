package com.vajrax.domain

import com.vajrax.core.time.TimeFormat
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Display follows the device 12/24-hour setting; stored values stay "HH:mm". */
class TimeFormatTest {
    @AfterTest
    fun reset() {
        TimeFormat.use24Hour = false
    }

    @Test
    fun twelveHourDisplay() {
        TimeFormat.use24Hour = false
        assertEquals("6:30 AM", TimeFormat.display("06:30"))
        assertEquals("12:00 AM", TimeFormat.display("00:00"))
        assertEquals("09:00 - 10:00 AM", TimeFormat.displayRange("09:00", 60))
        assertEquals("11:30 AM - 12:30 PM", TimeFormat.displayRange("11:30", 60))
    }

    @Test
    fun twentyFourHourDisplay() {
        TimeFormat.use24Hour = true
        assertEquals("06:30", TimeFormat.display("06:30"))
        assertEquals("18:05", TimeFormat.display("6:05 PM"))
        assertEquals("21:30 - 22:30", TimeFormat.displayRange("21:30", 60))
        assertEquals("23:30 - 00:30", TimeFormat.displayRange("23:30", 60))
    }
}
