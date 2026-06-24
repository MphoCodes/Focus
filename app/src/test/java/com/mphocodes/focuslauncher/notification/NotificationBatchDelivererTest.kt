package com.mphocodes.focuslauncher.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NotificationBatchDelivererTest {

    @Test
    fun testCurrentBatchToken_beforeNoon_returnsNull() {
        val now = LocalDateTime.of(2026, 6, 6, 11, 59)
        val token = NotificationBatchDeliverer.currentBatchToken(now)
        assertNull(token)
    }

    @Test
    fun testCurrentBatchToken_atNoon_returns12Token() {
        val now = LocalDateTime.of(2026, 6, 6, 12, 0)
        val token = NotificationBatchDeliverer.currentBatchToken(now)
        val expectedDate = now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        assertEquals("$expectedDate-12", token)
    }

    @Test
    fun testCurrentBatchToken_afterNoonBefore18_returns12Token() {
        val now = LocalDateTime.of(2026, 6, 6, 17, 59)
        val token = NotificationBatchDeliverer.currentBatchToken(now)
        val expectedDate = now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        assertEquals("$expectedDate-12", token)
    }

    @Test
    fun testCurrentBatchToken_at18_returns18Token() {
        val now = LocalDateTime.of(2026, 6, 6, 18, 0)
        val token = NotificationBatchDeliverer.currentBatchToken(now)
        val expectedDate = now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        assertEquals("$expectedDate-18", token)
    }

    @Test
    fun testCurrentBatchToken_after18_returns18Token() {
        val now = LocalDateTime.of(2026, 6, 6, 23, 59)
        val token = NotificationBatchDeliverer.currentBatchToken(now)
        val expectedDate = now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)
        assertEquals("$expectedDate-18", token)
    }
}
