package com.bernaferrari.sdkmonitor.main

import android.content.Context
import android.content.pm.PackageInfo
import androidx.paging.DataSource
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import kotlinx.coroutines.flow.Flow


interface MainDataSource {

  fun shouldOrderBySdk(): Flow<Boolean>
  suspend fun setShouldShowSystemApps(value: Boolean)

  suspend fun getAllVersions(packageName: String): List<Version>?

  suspend fun getAppsList(): List<App>

  suspend fun getLastItem(packageName: String): Version?

  suspend fun mapSdkDate(app: App): AppVersion

  suspend fun removePackageName(packageName: String)

  suspend fun getPackageInfo(packageName: String): PackageInfo?

  var forceRefresh: Boolean

  suspend fun getPackagesWithUserPrefs(): List<PackageInfo>

  suspend fun refreshAll(context: Context)

  suspend fun insertNewAppWithVersion(context: Context, packageInfo: PackageInfo)

  fun getVersionsPaged(): DataSource.Factory<Int, Version>

  suspend fun countNumberOfChanges(): Int
}
