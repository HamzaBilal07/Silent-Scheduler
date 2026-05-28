package com.mhamz.prayerdndmanager.domain

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DailyQuranAyahProviderTest {
    @Test
    fun dailyLibraryHasEnoughOfflineAyahsForVariety() {
        assertTrue(DailyQuranAyahProvider.librarySize >= 70)
    }

    @Test
    fun consecutiveDatesUsuallyShowDifferentAyahs() {
        val today = DailyQuranAyahProvider.today(LocalDate.of(2026, 5, 28))
        val tomorrow = DailyQuranAyahProvider.today(LocalDate.of(2026, 5, 29))

        assertNotEquals(today.reference, tomorrow.reference)
    }

    @Test
    fun sameDateAlwaysReturnsSameAyah() {
        val date = LocalDate.of(2026, 6, 1)

        assertTrue(DailyQuranAyahProvider.today(date) == DailyQuranAyahProvider.today(date))
    }
}
