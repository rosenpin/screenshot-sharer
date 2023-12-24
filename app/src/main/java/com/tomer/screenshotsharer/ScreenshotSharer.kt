package com.tomer.screenshotsharer

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.tomer.screenshotsharer.prefs.DB_NAME
import com.tomer.screenshotsharer.prefs.PrefsRepository
import com.tomer.screenshotsharer.prefs.PrefsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@HiltAndroidApp
class ScreenshotSharer :Application(){
}
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(DB_NAME, Context.MODE_PRIVATE)

    @Provides
    fun providePreferencesRepository(sharedPreferences: SharedPreferences): PrefsRepository =
        PrefsRepositoryImpl(sharedPreferences)
}