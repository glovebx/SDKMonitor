package com.bernaferrari.sdkmonitor.details

import com.airbnb.mvrx.MavericksViewModel
import com.bernaferrari.sdkmonitor.core.AppManager
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.data.source.local.VersionsDao
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.main.AppDetails
import com.bernaferrari.sdkmonitor.main.MainState
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailsViewModel @AssistedInject constructor(
    @Assisted state: MainState,
    private val mVersionsDao: VersionsDao
) : MavericksViewModel<MainState>(state) {

    suspend fun fetchAllVersions(packageName: String): List<Version>? =
        withContext(Dispatchers.IO) { mVersionsDao.getAllValues(packageName) }

    fun fetchAppDetails(packageName: String): MutableList<AppDetails> {

        val packageInfo = AppManager.getPackageInfo(packageName) ?: return mutableListOf()

        return mutableListOf<AppDetails>().apply {

            packageInfo.applicationInfo.className?.also {
                this += AppDetails("Class Name", it)
            }

            packageInfo.applicationInfo.sourceDir?.also {
                this += AppDetails("Source Dir", it)
            }

            packageInfo.applicationInfo.dataDir?.also {
                this += AppDetails("Data Dir", it)
            }
        }
    }

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<DetailsViewModel, MainState> {
        override fun create(state: MainState): DetailsViewModel
    }

    companion object : DaggerMavericksViewModelFactory<DetailsViewModel, MainState>(DetailsViewModel::class.java)
    
//    @AssistedInject.Factory
//    interface Factory {
//        fun create(state: MainState): DetailsViewModel
//    }
//
//    companion object : MvRxViewModelFactory<DetailsViewModel, MainState> {
//
//        override fun create(
//            viewModelContext: ViewModelContext,
//            state: MainState
//        ): DetailsViewModel? {
//            val fragment: DetailsDialog = (viewModelContext as FragmentViewModelContext).fragment()
//            return fragment.detailsViewModelFactory.create(state)
//        }
//    }
}
