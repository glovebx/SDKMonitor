package com.bernaferrari.sdkmonitor.main

import android.content.Context
import com.airbnb.mvrx.MavericksViewModel
import com.bernaferrari.sdkmonitor.core.AppManager
import com.bernaferrari.sdkmonitor.data.App
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.extensions.normalizeString
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

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
        combine(allApps(), inputRelay) {
            list, filter -> {
            list.takeIf { filter.isNotBlank() }
                    ?.filter { filter.normalizeString() in it.app.title.normalizeString() }
                    ?: list
            }.asFlow().execute { copy(listOfItems = it) }
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

    private fun allApps() = mainRepository.shouldOrderBySdk().flatMapLatest { orderBySdk ->
        mainRepository.getAppsList()
            .getAppsListObservable(orderBySdk)
    }

    private fun Flow<List<App>>.getAppsListObservable(orderBySdk: Boolean): Flow<List<AppVersion>> =
            this.map { list ->
                hasLoaded = true
                if (list.isNotEmpty()) {
                    delay(250L)
                }
                list
            }.dropWhile {
                if (it.isEmpty() || AppManager.forceRefresh) {
                    AppManager.forceRefresh = false
                    refreshAll()
                }
                it.isEmpty()
            }.map { list ->
                list.map { app -> mainRepository.mapSdkDate(app) }
            }.map { list ->
                if (orderBySdk) list.sortedBy { it.sdkVersion } else list
            }
//        this.debounce { list ->
//            // debounce with a 200ms delay on all items except the first one
//            val flow = Observable.just(list)
//            hasLoaded = true
//            if (list.isEmpty()) flow else flow.delay(250, TimeUnit.MILLISECONDS)
//        }.skipWhile {
//            // force the refresh when app is first opened or no known apps are installed (emulator)
//            if (it.isEmpty() || AppManager.forceRefresh) {
//                AppManager.forceRefresh = false
//                refreshAll()
//            }
//            it.isEmpty()
//        }.map { list ->
//            // parse correctly the values
//            list.map { app -> mainRepository.mapSdkDate(app) }
//        }.map { list ->
//            // list already comes sorted by name from db, it is faster and avoids sub-querying
//            if (orderBySdk) list.sortedBy { it.sdkVersion } else list
//        }.doOnNext { maxListSize.accept(it.size) }

    private fun refreshAll() {
        AppManager.getPackagesWithUserPrefs()
            // this condition will only happen when app there is no app installed
            // which means PROBABLY the app is being ran on emulator.
            .also {
                if (it.isEmpty()) mainRepository.setShouldShowSystemApps(true)
            }
            .forEach { packageInfo ->
                AppManager.insertNewApp(packageInfo)
                AppManager.insertNewVersion(context, packageInfo)
            }
    }

    @AssistedInject.Factory
    interface Factory : AssistedViewModelFactory<MainViewModel, MainState> {
        override fun create(state: MainState): MainViewModel
    }

    companion object : DaggerMavericksViewModelFactory<MainViewModel, MainState>(MainViewModel::class.java)
//    
//    @AssistedInject.Factory
//    interface Factory {
//        fun create(initialState: MainState): MainViewModel
//    }
//
//    companion object : MvRxViewModelFactory<MainViewModel, MainState> {
//
//        override fun create(
//            viewModelContext: ViewModelContext,
//            state: MainState
//        ): MainViewModel? {
//            val fragment: MainFragment = (viewModelContext as FragmentViewModelContext).fragment()
//            return fragment.mainViewModelFactory.create(state)
//        }
//    }
}
