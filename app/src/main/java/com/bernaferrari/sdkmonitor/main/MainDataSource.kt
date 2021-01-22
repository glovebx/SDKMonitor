package com.bernaferrari.sdkmonitor.main

import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.data.Version
import kotlinx.coroutines.flow.Flow


interface MainDataSource {

    fun setShouldShowSystemApps(value: Boolean)

    fun shouldOrderBySdk(): Flow<Boolean>

    fun getAppsList(): Flow<List<App>>

    fun getLastItem(packageName: String): Version?

    fun mapSdkDate(app: App): AppVersion
}
