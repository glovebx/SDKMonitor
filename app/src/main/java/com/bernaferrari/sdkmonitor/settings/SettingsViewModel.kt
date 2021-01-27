package com.bernaferrari.sdkmonitor.settings

import android.util.Log
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.afollestad.rxkprefs.rxjava.observe
import com.airbnb.mvrx.*
import com.bernaferrari.sdkmonitor.data.SettingsRepository
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.main.MainDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Named

data class SettingsData(
  val lightMode: Boolean,
  val showSystemApps: Boolean,
  val backgroundSync: Boolean,
  val orderBySdk: Boolean
) : MavericksState

data class SettingsState(
  val data: Async<SettingsData> = Loading()
) : MavericksState

class SettingsViewModel @AssistedInject constructor(
  @Assisted state: SettingsState,
  private val settingsRepository: SettingsRepository,
  private val mainRepository: MainDataSource,
  @Named(value = "lightMode") val lightMode: Pref<Boolean>,
  @Named(value = "showSystemApps") val showSystemApps: Pref<Boolean>,
  @Named(value = "backgroundSync") val backgroundSync: Pref<Boolean>,
  @Named(value = "orderBySdk") val orderBySdk: Pref<Boolean>
) : MavericksViewModel<SettingsState>(state) {

  init {
    fetchData()
  }

  private fun fetchData() = 
//    combine(
//            lightMode.asFlow(),
//            showSystemApps.asFlow(),
//            backgroundSync.asFlow(),
//            orderBySdk.asFlow()
//    ) { dark, system, backgroundSync, orderBySdk ->
//      SettingsData(dark, system, backgroundSync, orderBySdk)
//    }.execute { copy(data = it) }
//    viewModelScope.launch {
//      withContext(Dispatchers.IO + NonCancellable) {
//        settingsRepository.getSettings().execute {
//          copy(data = it)
//        }
//      }
//    }

      Observables.combineLatest(
              lightMode.observe(),
              showSystemApps.observe(),
              backgroundSync.observe(),
              orderBySdk.observe()
      ) { dark, system, backgroundSync, orderBySdk ->
        SettingsData(dark, system, backgroundSync, orderBySdk)
      }.subscribe {
        setState {
          copy(data = Success(it)) }
      }


//    viewModelScope.launch {
//      showSystemApps.asFlow().collect {
//                Log.i("showSystemApps","showSystemApps=$it")
//      }
//    }


  fun setLightTheme(isLightMode: Boolean) {
    settingsRepository.toggleLightTheme(isLightMode)
  }

  fun setShowSystemApps(isShowSystemApps: Boolean) {
    settingsRepository.toggleShowSystemApps(isShowSystemApps)
    mainRepository.forceRefresh = true
  }

  fun setOrderBySdk(isOrderBySdk: Boolean) {
    settingsRepository.toggleOrderBySdk(isOrderBySdk)
  }

  @AssistedInject.Factory
  interface Factory : AssistedViewModelFactory<SettingsViewModel, SettingsState> {
    override fun create(state: SettingsState): SettingsViewModel
  }

  companion object : DaggerMavericksViewModelFactory<SettingsViewModel, SettingsState>(SettingsViewModel::class.java)
//
//    @AssistedInject.Factory
//    interface Factory {
//        fun create(
//            state: SettingsState,
//            sources: Observable<SettingsData>
//        ): SettingsViewModel
//    }
//
//    companion object : MvRxViewModelFactory<SettingsViewModel, SettingsState> {
//
//        override fun create(
//            viewModelContext: ViewModelContext,
//            state: SettingsState
//        ): SettingsViewModel? {
//
//            val source = Observables.combineLatest(
//                Injector.get().isLightTheme().observe(),
//                Injector.get().showSystemApps().observe(),
//                Injector.get().backgroundSync().observe(),
//                Injector.get().orderBySdk().observe()
//            ) { dark, system, backgroundSync, orderBySdk ->
//                SettingsData(dark, system, backgroundSync, orderBySdk)
//            }
//
//            val fragment: SettingsFragment =
//                (viewModelContext as FragmentViewModelContext).fragment()
//            return fragment.settingsViewModelFactory.create(state, source)
//        }
//    }
}
