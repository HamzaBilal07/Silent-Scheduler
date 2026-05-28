package com.mhamz.prayerdndmanager.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrayerColorScheme = lightColorScheme(
    primary = Color(0xFF1B5E4B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7EFE6),
    onPrimaryContainer = Color(0xFF063829),
    secondary = Color(0xFF56635D),
    surface = Color(0xFFFBFCFB),
    onSurface = Color(0xFF1A1C1B),
    surfaceVariant = Color(0xFFE0E4E1),
    onSurfaceVariant = Color(0xFF444846),
    background = Color(0xFFF6F7F8),
    onBackground = Color(0xFF1A1C1B),
    error = Color(0xFFBA1A1A)
)

@Composable
fun PrayerSilentSchedulerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PrayerColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
