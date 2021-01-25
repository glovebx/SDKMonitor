package com.bernaferrari.sdkmonitor.main

import android.content.pm.PackageInfo
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import kotlinx.coroutines.flow.Flow


interface MainDataSource {

    suspend fun getAllVersions(packageName: String): List<Version>?

    suspend fun setShouldShowSystemApps(value: Boolean)

    suspend fun shouldOrderBySdk(): Flow<Boolean>

    suspend fun getAppsList(): Flow<List<App>>

    suspend fun getLastItem(packageName: String): Version?

    suspend fun mapSdkDate(app: App): AppVersion

    suspend fun removePackageName(packageName: String)

    suspend fun getPackageInfo(packageName: String): PackageInfo?
}
