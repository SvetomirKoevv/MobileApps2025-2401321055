package com.svetomir.spendly.di

import android.content.Context
import androidx.room.Room
import com.svetomir.spendly.data.local.AppDatabase
import com.svetomir.spendly.data.local.ExpenseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Replaces DatabaseModule in UI tests: each test gets a fresh in-memory database,
 * so tests start empty and never touch the real data on the phone.
 */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()
}
