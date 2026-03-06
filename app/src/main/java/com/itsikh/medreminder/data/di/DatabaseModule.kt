package com.itsikh.medreminder.data.di

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import com.itsikh.medreminder.data.db.MedicationDao
import com.itsikh.medreminder.data.db.MedicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): MedicationDatabase =
        Room.databaseBuilder(ctx, MedicationDatabase::class.java, "medication_db").build()

    @Provides @Singleton
    fun provideDao(db: MedicationDatabase): MedicationDao = db.medicationDao()

    @Provides @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
