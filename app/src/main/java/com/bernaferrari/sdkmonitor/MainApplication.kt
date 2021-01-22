package com.bernaferrari.sdkmonitor

import android.app.Application
import com.afollestad.rxkprefs.Pref
import com.airbnb.mvrx.Mavericks
import com.bernaferrari.sdkmonitor.core.AppManager
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import com.bernaferrari.sdkmonitor.data.source.local.VersionsDao
import com.facebook.stetho.Stetho
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.squareup.leakcanary.LeakCanary
import dagger.hilt.android.HiltAndroidApp
import io.karn.notify.NotifyCreator
import javax.inject.Inject
import javax.inject.Named

@HiltAndroidApp
class MainApplication : Application() {

//    @Inject
//    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Any>
//
//    override fun androidInjector(): AndroidInjector<Any>? {
//        return fragmentDispatchingAndroidInjector
//    }

//    lateinit var component: SingletonComponent
    @Inject lateinit var notifyCreator: NotifyCreator
    @Inject lateinit var appsDao: AppsDao
    @Inject lateinit var versionsDao: VersionsDao
    @Inject
    @Named(value = "showSystemApps") lateinit var showSystemApps: Pref<Boolean>

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        Mavericks.initialize(this)
//
//        component = DaggerSingletonComponent.builder()
//            .application(this)
//            .build()
//            .also { it.inject(this) }

        Logger.addLogAdapter(object : AndroidLogAdapter() {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return BuildConfig.DEBUG
            }
        })

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }

        AppManager.init(this
                , notifyCreator
                , appsDao
                , versionsDao
                , showSystemApps)
    }

    companion object {
        private var INSTANCE: MainApplication? = null

        @JvmStatic
        fun get(): MainApplication =
            INSTANCE ?: throw NullPointerException("MainApplication INSTANCE must not be null")
    }
}