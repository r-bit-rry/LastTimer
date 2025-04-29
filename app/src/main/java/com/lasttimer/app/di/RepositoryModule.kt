package com.lasttimer.app.di

import com.lasttimer.app.data.dao.TimerDao
import com.lasttimer.app.data.dao.TimerGroupDao
import com.lasttimer.app.data.dao.TimerLapDao
import com.lasttimer.app.data.repository.ITimerRepository
import com.lasttimer.app.data.repository.TimerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideTimerRepository(
        timerDao: TimerDao,
        timerGroupDao: TimerGroupDao,
        timerLapDao: TimerLapDao
    ): ITimerRepository {
        return TimerRepository(timerDao, timerGroupDao, timerLapDao)
    }
}
