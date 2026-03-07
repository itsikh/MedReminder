package com.itsikh.medreminder.data.di

import android.app.AlarmManager
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.itsikh.medreminder.data.db.MedicationDao
import com.itsikh.medreminder.data.db.MedicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE medications ADD COLUMN stockQuantity INTEGER NOT NULL DEFAULT -1")
        database.execSQL("ALTER TABLE medications ADD COLUMN stockInitial INTEGER NOT NULL DEFAULT -1")
        database.execSQL("ALTER TABLE medications ADD COLUMN lowStockThresholdPct INTEGER NOT NULL DEFAULT 20")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): MedicationDatabase =
        Room.databaseBuilder(ctx, MedicationDatabase::class.java, "medication_db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides @Singleton
    fun provideDao(db: MedicationDatabase): MedicationDao = db.medicationDao()

    @Provides @Singleton
    fun provideAlarmManager(@ApplicationContext ctx: Context): AlarmManager =
        ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}
