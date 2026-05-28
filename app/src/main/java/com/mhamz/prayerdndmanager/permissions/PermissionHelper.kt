package com.mhamz.prayerdndmanager.permissions

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Immutable
import androidx.core.content.ContextCompat

@Immutable
data class PermissionSnapshot(
    val dndAccessGranted: Boolean,
    val exactAlarmGranted: Boolean,
    val notificationGranted: Boolean,
    val locationGranted: Boolean
) {
    val allRequiredGranted: Boolean = dndAccessGranted && exactAlarmGranted && notificationGranted && locationGranted
}

enum class PermissionTarget {
    DND_ACCESS,
    EXACT_ALARM,
    NOTIFICATIONS,
    LOCATION
}

object PermissionHelper {
    fun snapshot(context: Context): PermissionSnapshot {
        return PermissionSnapshot(
            dndAccessGranted = hasDndAccess(context),
            exactAlarmGranted = canScheduleExactAlarms(context),
            notificationGranted = hasNotificationPermission(context),
            locationGranted = hasLocationPermission(context)
        )
    }

    fun hasDndAccess(context: Context): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        return manager?.isNotificationPolicyAccessGranted == true
    }

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.getSystemService(AlarmManager::class.java)
        return manager?.canScheduleExactAlarms() == true
    }

    fun hasNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun nextMissingPermission(snapshot: PermissionSnapshot): PermissionTarget? {
        return when {
            !snapshot.dndAccessGranted -> PermissionTarget.DND_ACCESS
            !snapshot.exactAlarmGranted -> PermissionTarget.EXACT_ALARM
            !snapshot.notificationGranted -> PermissionTarget.NOTIFICATIONS
            !snapshot.locationGranted -> PermissionTarget.LOCATION
            else -> null
        }
    }

    fun dndSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
    }

    fun exactAlarmSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            appSettingsIntent(context)
        }
    }

    fun appNotificationSettingsIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            appSettingsIntent(context)
        }
    }

    fun appSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}
