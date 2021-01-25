package com.bernaferrari.sdkmonitor.main

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.bernaferrari.sdkmonitor.MainActivity
import com.bernaferrari.sdkmonitor.R
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import com.bernaferrari.sdkmonitor.data.source.local.VersionsDao
import com.bernaferrari.sdkmonitor.extensions.convertTimestampToDate
import com.bernaferrari.sdkmonitor.extensions.darken
import dagger.hilt.android.qualifiers.ApplicationContext
import io.karn.notify.Notify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseDataSource @Inject constructor(
        @ApplicationContext context: Context,
        private val versionsDao: VersionsDao,
        private val appsDao: AppsDao,
        private val orderBySdk: Pref<Boolean>,
        private val showSystemApps: Pref<Boolean>
) : MainDataSource {
    var ioDispatcher = Dispatchers.IO

    private var packageManager: PackageManager = context.packageManager
    var forceRefresh = true

    override suspend fun getAllVersions(packageName: String): List<Version>? = withContext(ioDispatcher) {
        versionsDao.getAllValues(packageName)
    }

    override suspend fun setShouldShowSystemApps(value: Boolean) {
        showSystemApps.set(value)
    }

    override suspend fun shouldOrderBySdk(): Flow<Boolean> = orderBySdk.asFlow()

    override suspend fun getLastItem(packageName: String): Version? = versionsDao.getLastValue(packageName)

    override suspend fun getAppsList(): Flow<List<App>> =
        // return only the ones from Play Store or that were installed manually.// return all apps
        withContext(ioDispatcher) {
            showSystemApps.asFlow().flatMapLatest { systemApps ->
                if (systemApps) {
                    // return all apps
                    appsDao.getAppsListFlowable()
                } else {
                    // return only the ones from Play Store or that were installed manually.
                    appsDao.getAppsListFlowableFiltered(hasKnownOrigin = true)
                }
            }

//        return showSystemApps.asFlow().flatMapLatest { systemApps ->
//            if (systemApps) {
//                // return all apps
//                mAppsDao.getAppsListFlowable()
//            } else {
//                // return only the ones from Play Store or that were installed manually.
//                mAppsDao.getAppsListFlowableFiltered(hasKnownOrigin = true)
//            }
//        }
    }

    override suspend fun mapSdkDate(app: App): AppVersion {
        // since insertApp is called before insertVersion, mVersionsDao.getValue(...) will
        // return null on app's first run. This will avoid the situation.
        return try {
            val version = versionsDao.getLastValue(app.packageName)

            val sdkVersion =
                    version?.targetSdk
                            ?: getApplicationInfo(app.packageName)?.targetSdkVersion
                            ?: 0

            val lastUpdate =
                    version?.lastUpdateTime
                            ?: getPackageInfo(app.packageName)?.lastUpdateTime
                            ?: 0

            AppVersion(app, sdkVersion, lastUpdate.convertTimestampToDate())
        } catch (ex: Throwable) {
            throw ex
        }
    }


    private fun isUserApp(ai: ApplicationInfo?): Boolean {
        // https://stackoverflow.com/a/14665381/4418073
        if (ai == null) return false
        val mask = ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        return ai.flags and mask == 0
    }

    // verifies if app came from Play Store or was installed manually
    suspend fun doesAppHasOrigin(packageName: String): Boolean {
        return isUserApp(getApplicationInfo(packageName))
    }

    private fun getPackages(): List<PackageInfo> =
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)

    override suspend fun removePackageName(packageName: String) = withContext(ioDispatcher) {
//        Injector.get().appsDao().deleteApp(packageName)
        appsDao.deleteApp(packageName)
    }

    fun insertNewVersion(context: Context, packageInfo: PackageInfo) {

        val versionCode =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }

        val currentTargetSDK = packageInfo.applicationInfo.targetSdkVersion

//        val lastVersion = Injector.get().versionsDao().getLastTargetSDK(packageInfo.packageName)
        val lastVersion = versionsDao.getLastTargetSDK(packageInfo.packageName)

        if (lastVersion != currentTargetSDK) {

            val version = Version(
                    version = versionCode,
                    packageName = packageInfo.packageName,
                    versionName = packageInfo.versionName ?: "",
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    targetSdk = currentTargetSDK
            )

//            Injector.get().versionsDao().insertVersion(version)
            versionsDao.insertVersion(version)

            if (lastVersion != null) {
//                val appContext = Injector.get().appContext()

                Notify.with(context)
                        .header { this.icon = R.drawable.ic_target }
                        .meta {
                            this.clickIntent = PendingIntent.getActivity(
                                    context, 0,
                                    Intent(context, MainActivity::class.java), 0
                            )
                        }.content {
                            title = "TargetSDK changed for ${getAppLabel(packageInfo)}!"
                            text = "Went from $lastVersion to $currentTargetSDK"
                        }
                        .show()
            }
        }
    }

    // there are apps with extra space on the name
    private fun getAppLabel(packageInfo: PackageInfo) =
            packageManager.getApplicationLabel(packageInfo.applicationInfo).toString().trim()

    fun insertNewApp(packageInfo: PackageInfo) {

//        if (Injector.get().appsDao().getAppString(packageInfo.packageName) != null) return
        if (appsDao.getAppString(packageInfo.packageName) != null) return

        val icon = packageManager.getApplicationIcon(packageInfo.applicationInfo).toBitmap()
        val backgroundColor = getPaletteColor(Palette.from(icon).generate())
        val label = getAppLabel(packageInfo)

//        Injector.get().appsDao().insertApp(
        appsDao.insertApp(
                App(
                        packageName = packageInfo.packageName,
                        title = label,
                        backgroundColor = backgroundColor,
                        isFromPlayStore = isUserApp(packageInfo.applicationInfo)
                )
        )
    }

    private fun getPaletteColor(palette: Palette?, defaultColor: Int = 0) = when {
        palette?.darkVibrantSwatch != null -> palette.getDarkVibrantColor(defaultColor)
        palette?.vibrantSwatch != null -> palette.getVibrantColor(defaultColor)
        palette?.mutedSwatch != null -> palette.getMutedColor(defaultColor)
        palette?.darkMutedSwatch != null -> palette.getDarkMutedColor(defaultColor)
        palette?.lightMutedSwatch != null -> palette.getMutedColor(defaultColor).darken
        palette?.lightVibrantSwatch != null -> palette.getLightVibrantColor(defaultColor).darken
        else -> defaultColor
    }

    fun getPackagesWithUserPrefs(): List<PackageInfo> {
//        return if (Injector.get().showSystemApps().get()) {
        return if (showSystemApps.get()) {
            getPackages()
        } else {
            getPackagesWithOrigin()
        }
    }

    private fun getPackagesWithOrigin(): List<PackageInfo> {
        return packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
                .filter { isUserApp(it.applicationInfo) }
    }

    override suspend fun getPackageInfo(packageName: String): PackageInfo? = withContext(ioDispatcher) {
        try {
            packageManager.getPackageInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private suspend fun getApplicationInfo(packageName: String): ApplicationInfo? {
        return getPackageInfo(packageName)?.applicationInfo
    }

    suspend fun getIconFromId(packageName: String): Drawable? {
        return try {
            packageManager.getApplicationIcon(getApplicationInfo(packageName)!!)
        } catch (e: Exception) {
            null
        }
    }
}
