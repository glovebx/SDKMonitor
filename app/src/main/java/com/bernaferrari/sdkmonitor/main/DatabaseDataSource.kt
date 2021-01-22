package com.bernaferrari.sdkmonitor.main

import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.bernaferrari.sdkmonitor.core.AppManager
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import com.bernaferrari.sdkmonitor.data.source.local.VersionsDao
import com.bernaferrari.sdkmonitor.extensions.convertTimestampToDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseDataSource @Inject constructor(
    private val mVersionsDao: VersionsDao,
    private val mAppsDao: AppsDao,
    private val orderBySdk: Pref<Boolean>,
    private val showSystemApps: Pref<Boolean>
) : MainDataSource {

    override fun setShouldShowSystemApps(value: Boolean) {
        showSystemApps.set(value)
    }

    override fun shouldOrderBySdk(): Flow<Boolean> = orderBySdk.asFlow()

    override fun getLastItem(packageName: String): Version? = mVersionsDao.getLastValue(packageName)

    override fun getAppsList(): Flow<List<App>> {

        return showSystemApps.asFlow().flatMapLatest { systemApps ->
            if (systemApps) {
                // return all apps
                mAppsDao.getAppsListFlowable()
            } else {
                // return only the ones from Play Store or that were installed manually.
                mAppsDao.getAppsListFlowableFiltered(hasKnownOrigin = true)
            }
        }
    }

    override fun mapSdkDate(app: App): AppVersion {

        // since insertApp is called before insertVersion, mVersionsDao.getValue(...) will
        // return null on app's first run. This will avoid the situation.
        val version = mVersionsDao.getLastValue(app.packageName)

        val sdkVersion =
            version?.targetSdk
                    ?: AppManager.getApplicationInfo(app.packageName)?.targetSdkVersion
                    ?: 0

        val lastUpdate =
            version?.lastUpdateTime
                    ?: AppManager.getPackageInfo(app.packageName)?.lastUpdateTime
                    ?: 0

        return AppVersion(app, sdkVersion, lastUpdate.convertTimestampToDate())
    }

}
