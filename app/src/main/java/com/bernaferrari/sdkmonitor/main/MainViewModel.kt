package com.bernaferrari.sdkmonitor.main

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

class MainViewModel @AssistedInject constructor(
  @Assisted state: MainState,
  @ApplicationContext private val context: Context,
  private val mainRepository: MainDataSource
) : MavericksViewModel<MainState>(state) {

  val itemsList = mutableListOf<AppVersion>()
  var hasLoaded = false
  var maxListSize: MutableSharedFlow<Int> = MutableSharedFlow()
  val inputRelay: MutableSharedFlow<String> = MutableSharedFlow()

  init {
    fetchData()
  }

  private fun fetchData() = withState {
    allApps().execute {
      copy(listOfItems = it)
    }

//
//        Observables.combineLatest(
//            allApps(),
//            inputRelay
//        ) { list, filter ->
//
//            // get the string without special characters and filter the list.
//            // If the filter is not blank, it will filter the list.
//            // If it is blank, it will return the original list.
//            list.takeIf { filter.isNotBlank() }
//                ?.filter { filter.normalizeString() in it.app.title.normalizeString() }
//                    ?: list
//        }.doOnNext {
//            itemsList.clear()
//            itemsList.addAll(it)
//        }.execute {
//            copy(listOfItems = it)
//        }
  }

  private fun allApps() = viewModelScope.async {
    if (mainRepository.forceRefresh) {
      mainRepository.forceRefresh = false
      mainRepository.refreshAll(context)
    }
    val appList = mainRepository.getAppsList()

    maxListSize.tryEmit(appList.size)
    val versionList = appList.map { app -> mainRepository.mapSdkDate(app) }

    var orderBySdk = false
//    mainRepository.shouldOrderBySdk().collect {
//      orderBySdk = it
//    }
    if (orderBySdk) versionList.sortedBy { it.sdkVersion } else versionList
  }

  @AssistedInject.Factory
  interface Factory : AssistedViewModelFactory<MainViewModel, MainState> {
    override fun create(state: MainState): MainViewModel
  }

  companion object : DaggerMavericksViewModelFactory<MainViewModel, MainState>(MainViewModel::class.java)
}
