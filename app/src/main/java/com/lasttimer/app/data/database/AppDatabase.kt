package com.lasttimer.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lasttimer.app.data.converter.DateConverter
import com.lasttimer.app.data.converter.TimerTypeConverter
import com.lasttimer.app.data.dao.TimerDao
import com.lasttimer.app.data.dao.TimerGroupDao
import com.lasttimer.app.data.dao.TimerLapDao
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerLap

@Database(
    entities = [
        Timer::class,
        TimerGroup::class,
        TimerGroupItem::class,
        TimerLap::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TimerTypeConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timerDao(): TimerDao
    abstract fun timerGroupDao(): TimerGroupDao
    abstract fun timerLapDao(): TimerLapDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lasttimer_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
