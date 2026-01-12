package com.bdavidgm.consumoelectrico.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.bdavidgm.consumoelectrico.room.ConsumoDao
import com.bdavidgm.consumoelectrico.room.ConsumoDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val SETTINGS_DATASTORE_NAME = "app_settings"

// Define a top-level property for the delegate (optional, for direct usage)
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = SETTINGS_DATASTORE_NAME)

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesConsumoDao(consumoDataBase: ConsumoDataBase) : ConsumoDao {
        return consumoDataBase.consumoDao()
    }

    @Singleton
    @Provides
    fun providesConsumoDataBase(@ApplicationContext context : Context): ConsumoDataBase {
        return Room.databaseBuilder(
            context = context,
            ConsumoDataBase::class.java,
            name = "Consumo"
        ).build()
    }


    @Provides
    @Singleton // Ensures only one DataStore instance exists[citation:6]
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        // Use the delegate or create it explicitly
        return context.settingsDataStore
    }

   /* @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }*/



}