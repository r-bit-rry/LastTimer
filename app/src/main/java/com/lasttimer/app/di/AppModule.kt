package com.lasttimer.app.di

import android.content.Context
import com.lasttimer.app.data.dao.TimerDao
import com.lasttimer.app.data.dao.TimerGroupDao
import com.lasttimer.app.data.dao.TimerLapDao
import com.lasttimer.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideTimerDao(database: AppDatabase): TimerDao {
        return database.timerDao()
    }
    
    @Provides
    fun provideTimerGroupDao(database: AppDatabase): TimerGroupDao {
        return database.timerGroupDao()
    }
    
    @Provides
    fun provideTimerLapDao(database: AppDatabase): TimerLapDao {
        return database.timerLapDao()
    }
}
