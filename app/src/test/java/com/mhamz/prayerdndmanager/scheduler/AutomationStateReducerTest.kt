package com.mhamz.prayerdndmanager.scheduler

import com.mhamz.prayerdndmanager.domain.AutomationState
import com.mhamz.prayerdndmanager.domain.AutomationStateReducer
import com.mhamz.prayerdndmanager.domain.SoundSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AutomationStateReducerTest {
    @Test
    fun overlappingPrayersRestoreOnlyAfterTheLastPrayerEnds() {
        val firstMode = SoundSnapshot(interruptionFilter = 1, ringerMode = 2)
        val secondMode = SoundSnapshot(interruptionFilter = 3, ringerMode = 4)

        val afterAStarts = AutomationStateReducer.startPrayer(AutomationState(), 10, firstMode)
        val afterBStarts = AutomationStateReducer.startPrayer(afterAStarts, 20, secondMode)
        val afterAEnds = AutomationStateReducer.endPrayer(afterBStarts, 10)
        val afterBEnds = AutomationStateReducer.endPrayer(afterAEnds, 20)

        assertEquals(setOf(10L, 20L), afterBStarts.activePrayerIds)
        assertEquals(1, afterBStarts.previousInterruptionFilter)
        assertEquals(2, afterBStarts.previousRingerMode)
        assertEquals(setOf(20L), afterAEnds.activePrayerIds)
        assertTrue(afterBEnds.activePrayerIds.isEmpty())
        assertNull(afterBEnds.previousInterruptionFilter)
        assertNull(afterBEnds.previousRingerMode)
    }
}
