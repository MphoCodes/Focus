package com.mphocodes.focuslauncher

import com.mphocodes.focuslauncher.data.model.QuotaPresets
import org.junit.Assert.assertEquals
import org.junit.Test

class QuotaBlockTest {

    @Test
    fun quotaPresets_cycleThroughExpectedLimits() {
        assertEquals(15, QuotaPresets.next(0))
        assertEquals(30, QuotaPresets.next(15))
        assertEquals(60, QuotaPresets.next(30))
        assertEquals(120, QuotaPresets.next(60))
        assertEquals(0, QuotaPresets.next(120))
    }

    @Test
    fun quotaPresets_unknownLimitResetsToOff() {
        assertEquals(0, QuotaPresets.next(45))
    }
}
