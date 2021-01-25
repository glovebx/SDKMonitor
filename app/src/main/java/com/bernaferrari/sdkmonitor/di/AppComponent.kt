package com.bernaferrari.sdkmonitor.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.RxkPrefs
import com.afollestad.rxkprefs.rxkPrefs
import com.bernaferrari.sdkmonitor.data.source.local.AppDatabase
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import com.bernaferrari.sdkmonitor.data.source.local.VersionsDao
import com.bernaferrari.sdkmonitor.main.DatabaseDataSource
import com.bernaferrari.sdkmonitor.main.MainDataSource
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import io.karn.notify.Notify
import io.karn.notify.NotifyCreator
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppComponent {

  @Provides
  fun provideSharedPreferences(@ApplicationContext application: Context): SharedPreferences {
    return application.getSharedPreferences("workerPreferences", Context.MODE_PRIVATE)
  }

  @Provides
  fun provideRxPrefs(sharedPreferences: SharedPreferences): RxkPrefs {
    return rxkPrefs(sharedPreferences)
  }

  @Provides
  @Named("lightMode")
  fun provideIsLightTheme(rxPrefs: RxkPrefs): Pref<Boolean> {
    return rxPrefs.boolean("lightMode", true)
  }

  @Provides
  @Named("showSystemApps")
  fun provideShowSystemApps(rxPrefs: RxkPrefs): Pref<Boolean> {
    return rxPrefs.boolean("showSystemApps", false)
  }

  @Provides
  @Named("backgroundSync")
  fun provideBackgroundSync(rxPrefs: RxkPrefs): Pref<Boolean> {
    return rxPrefs.boolean("backgroundSync", false)
  }

  @Provides
  @Named("syncInterval")
  fun provideSyncInterval(rxPrefs: RxkPrefs): Pref<String> {
    return rxPrefs.string("syncInterval", "301")
  }

  @Provides
  @Named("orderBySdk")
  fun provideOrderBySdk(rxPrefs: RxkPrefs): Pref<Boolean> {
    return rxPrefs.boolean("orderBySdk", false)
  }

  @Singleton
  @Provides
  internal fun provideDb(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
      context.applicationContext,
      AppDatabase::class.java,
      "Apps.db"
    )
      .fallbackToDestructiveMigration()
      .build()
  }

  @Singleton
  @Provides
  internal fun provideAppsDao(db: AppDatabase): AppsDao = db.snapsDao()

  @Singleton
  @Provides
  internal fun provideVersionsDao(db: AppDatabase): VersionsDao = db.versionsDao()

  @Provides
  fun provideDictRepository(
    @ApplicationContext context: Context,
    versionsDao: VersionsDao,
    appsDao: AppsDao,
    @Named(value = "orderBySdk") orderBySdk: Pref<Boolean>,
    @Named(value = "showSystemApps") showSystemApps: Pref<Boolean>
  ): MainDataSource = DatabaseDataSource(context, versionsDao, appsDao, orderBySdk, showSystemApps)

  @Provides
  fun provideNotifyCreator(@ApplicationContext context: Context): NotifyCreator {
    return Notify.with(context)
  }
}
