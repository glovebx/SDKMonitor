package com.bernaferrari.sdkmonitor.core

import android.content.Context
import androidx.work.*
import com.bernaferrari.sdkmonitor.WorkerHelper.SERVICEWORK
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class PackageService(
    val context: Context,
    workerParameters: WorkerParameters
) : Worker(context, workerParameters) {

    override fun doWork(): Result {
        val action = inputData.getString("action")
        val packageName = inputData.getString(EXTRA_PACKAGE_NAME) ?: ""

        when (action) {
            ACTION_FETCH_INSERT -> handleActionInsert(packageName)
            ACTION_FETCH_UPDATE -> handleActionFetchUpdate(packageName)
            ACTION_REMOVE_PACKAGE -> handleActionRemovePackage(packageName)
        }

        return Result.success()
    }

    @Inject lateinit var appsDao: AppsDao

    private fun handleActionRemovePackage(packageName: String) {
//        Injector.get().appsDao().deleteApp(packageName)
        appsDao.deleteApp(packageName)
    }

    private fun handleActionFetchUpdate(packageName: String) = runBlocking {
        if (AppManager.doesAppHasOrigin(packageName)) {
            val packageInfo = AppManager.getPackageInfo(packageName) ?: return@runBlocking
            AppManager.insertNewVersion(context, packageInfo)
        }
    }

    private fun handleActionInsert(packageName: String) = runBlocking {
        if (AppManager.doesAppHasOrigin(packageName)) {
            val packageInfo = AppManager.getPackageInfo(packageName) ?: return@runBlocking
            AppManager.insertNewApp(packageInfo)
            AppManager.insertNewVersion(context, packageInfo)
        }
    }

    companion object {

        private const val ACTION_REMOVE_PACKAGE = "REMOVE_PACKAGE"
        private const val ACTION_FETCH_UPDATE = "FETCH_UPDATE"
        private const val ACTION_FETCH_INSERT = "FETCH_INSERT"
        private const val EXTRA_PACKAGE_NAME = "PACKAGE_NAME"

        fun startActionRemovePackage(context: Context, packageName: String) {
            enqueueWork(ACTION_REMOVE_PACKAGE, packageName)
        }

        fun startActionFetchUpdate(packageName: String) {
            enqueueWork(ACTION_FETCH_UPDATE, packageName)
        }

        fun startActionAddPackage(packageName: String) {
            enqueueWork(ACTION_FETCH_INSERT, packageName)
        }

        private fun enqueueWork(action: String, packageName: String) {
            val inputData = Data.Builder()
                .putString("action", action)
                .putString(EXTRA_PACKAGE_NAME, packageName)
                .build()

            val work = OneTimeWorkRequest.Builder(PackageService::class.java)
                .addTag(SERVICEWORK)
                .setInputData(inputData)
                .build()

            WorkManager.getInstance().enqueue(work)
        }
    }
}
