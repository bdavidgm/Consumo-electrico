package com.bdavidgm.consumoelectrico.di

import android.content.Context
import androidx.room.Room
import com.bdavidgm.consumoelectrico.room.ConsumoDao
import com.bdavidgm.consumoelectrico.room.ConsumoDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


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

}