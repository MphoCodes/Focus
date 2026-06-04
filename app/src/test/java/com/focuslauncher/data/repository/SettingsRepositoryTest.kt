package com.focuslauncher.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryTest {

    @Test
    fun testDecodeLaunchCounts_validInput() {
        val encoded = setOf("com.facebook.katana=15", "com.instagram.android=42")
        val decoded = LaunchCountSerializer.decode(encoded)

        assertEquals(2, decoded.size)
        assertEquals(15, decoded["com.facebook.katana"])
        assertEquals(42, decoded["com.instagram.android"])
    }

    @Test
    fun testDecodeLaunchCounts_invalidInput_ignored() {
        val encoded = setOf(
            "invalid_format",       // No '=' separator
            "=5",                   // Missing package name
            "com.pkg=",             // Missing count
            "com.pkg=abc",          // Count is not an integer
            "com.pkg.equal=name=5"  // Double '=' separator (handled properly by lastIndexOf)
        )
        val decoded = LaunchCountSerializer.decode(encoded)

        // Only "com.pkg.equal=name=5" should decode correctly to ("com.pkg.equal=name" to 5)
        assertEquals(1, decoded.size)
        assertEquals(5, decoded["com.pkg.equal=name"])
    }

    @Test
    fun testEncodeLaunchCounts_validInput() {
        val counts = mapOf(
            "com.facebook.katana" to 15,
            "com.instagram.android" to 42,
            "com.zero.app" to 0,       // Should be filtered out
            "com.negative.app" to -5   // Should be filtered out
        )
        val encoded = LaunchCountSerializer.encode(counts)

        assertEquals(2, encoded.size)
        assertTrue(encoded.contains("com.facebook.katana=15"))
        assertTrue(encoded.contains("com.instagram.android=42"))
    }

    @Test
    fun testEncodeLaunchCounts_emptyMap() {
        val encoded = LaunchCountSerializer.encode(emptyMap())
        assertTrue(encoded.isEmpty())
    }

    @Test
    fun testDecodeAppDisguises_validInput() {
        val appId = "com.instagram.android/.MainActivity"
        val encoded = setOf("${appId.length}:${appId}Endless Scrolling")
        val decoded = AppDisguiseSerializer.decode(encoded)

        assertEquals(1, decoded.size)
        assertEquals("Endless Scrolling", decoded[appId])
    }

    @Test
    fun testDecodeAppDisguises_invalidInput_ignored() {
        val encoded = setOf(
            "invalid",
            "abc:com.appAlias",
            "0:Alias",
            "99:shortAlias",
            "8:com.app "
        )
        val decoded = AppDisguiseSerializer.decode(encoded)

        assertTrue(decoded.isEmpty())
    }

    @Test
    fun testEncodeAppDisguises_filtersBlankValues() {
        val encoded = AppDisguiseSerializer.encode(
            mapOf(
                "com.instagram.android/.MainActivity" to " Endless Scrolling ",
                "com.blank/.Main" to " ",
                "" to "Tedious Task"
            )
        )

        assertEquals(1, encoded.size)
        assertEquals(
            mapOf("com.instagram.android/.MainActivity" to "Endless Scrolling"),
            AppDisguiseSerializer.decode(encoded)
        )
    }
}
