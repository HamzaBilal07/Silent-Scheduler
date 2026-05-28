package com.mhamz.prayerdndmanager.data

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [PrayerScheduleEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun prayerScheduleDao(): PrayerScheduleDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prayer_schedules ADD COLUMN repeatDaysMask INTEGER NOT NULL DEFAULT 127")
                db.execSQL(
                    """
                    UPDATE prayer_schedules
                    SET repeatDaysMask = CASE
                        WHEN fridayOnly = 1 THEN 16
                        WHEN repeatDaily = 1 THEN 127
                        ELSE 0
                    END
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Remove only untouched seed rows from older installs. Edited, enabled, or custom rows stay intact.
                db.execSQL(
                    """
                    DELETE FROM prayer_schedules
                    WHERE enabled = 0
                    AND createdAt = updatedAt
                    AND (
                        (name = 'Lecture' AND startHour = 9 AND startMinute = 0 AND endHour = 10 AND endMinute = 0 AND repeatDaily = 0 AND fridayOnly = 0 AND repeatDaysMask = 0)
                        OR (name = 'Fajr' AND startHour = 5 AND startMinute = 10 AND endHour = 5 AND endMinute = 40 AND repeatDaily = 1 AND fridayOnly = 0 AND repeatDaysMask = 127)
                        OR (name = 'Zuhr' AND startHour = 13 AND startMinute = 30 AND endHour = 14 AND endMinute = 0 AND repeatDaily = 1 AND fridayOnly = 0 AND repeatDaysMask = 127)
                        OR (name = 'Asr' AND startHour = 17 AND startMinute = 15 AND endHour = 17 AND endMinute = 45 AND repeatDaily = 1 AND fridayOnly = 0 AND repeatDaysMask = 127)
                        OR (name = 'Maghrib' AND startHour = 19 AND startMinute = 5 AND endHour = 19 AND endMinute = 25 AND repeatDaily = 1 AND fridayOnly = 0 AND repeatDaysMask = 127)
                        OR (name = 'Isha' AND startHour = 20 AND startMinute = 45 AND endHour = 21 AND endMinute = 15 AND repeatDaily = 1 AND fridayOnly = 0 AND repeatDaysMask = 127)
                        OR (name = 'Jummah' AND startHour = 13 AND startMinute = 15 AND endHour = 14 AND endMinute = 0 AND repeatDaily = 0 AND fridayOnly = 1 AND repeatDaysMask = 16)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Older versions could auto-update the seeded disabled prayer rows before this app update.
                // Remove disabled legacy default-name rows so users start from their own event schedules.
                db.execSQL(
                    """
                    DELETE FROM prayer_schedules
                    WHERE enabled = 0
                    AND name IN ('Lecture', 'Fajr', 'Zuhr', 'Asr', 'Maghrib', 'Isha', 'Jummah')
                    """.trimIndent()
                )
            }
        }
    }
}
