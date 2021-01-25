package com.bernaferrari.sdkmonitor.details

import com.airbnb.mvrx.*
import com.bernaferrari.sdkmonitor.data.Version
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.main.AppDetails
import com.bernaferrari.sdkmonitor.main.MainDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class DetailsState(
        val packageName: String = "",
        val listOfVersions: Async<List<Version>?> = Loading(),
        val listOfDetails: Async<List<AppDetails>> = Loading()
) : MavericksState

class DetailsViewModel @AssistedInject constructor(
        @Assisted state: DetailsState,
        private val repository: MainDataSource
) : MavericksViewModel<DetailsState>(state) {

    fun fetchAllVersions(packageName: String) = viewModelScope.async {
        repository.getAllVersions(packageName)
    }.execute { copy(listOfVersions = it) }

    fun fetchAppDetails(packageName: String) = viewModelScope.async {
        repository.getPackageInfo(packageName)?.let {
            mutableListOf<AppDetails>().apply {
                it.applicationInfo.className?.also {
                    this += AppDetails("Class Name", it)
                }

                it.applicationInfo.sourceDir?.also {
                    this += AppDetails("Source Dir", it)
                }

                it.applicationInfo.dataDir?.also {
                    this += AppDetails("Data Dir", it)
                }
            }

        } ?: mutableListOf<AppDetails>()
    }.execute { copy(packageName = packageName, listOfDetails = it) }

    fun removePackageName(packageName: String) = viewModelScope.launch {
        repository.removePackageName(packageName)
    }

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<DetailsViewModel, DetailsState> {
        override fun create(state: DetailsState): DetailsViewModel
    }

    companion object : DaggerMavericksViewModelFactory<DetailsViewModel, DetailsState>(DetailsViewModel::class.java)

}
