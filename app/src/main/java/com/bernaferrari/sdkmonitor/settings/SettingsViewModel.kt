package com.bernaferrari.sdkmonitor.settings

import com.airbnb.mvrx.*
import com.bernaferrari.sdkmonitor.data.SettingsRepository
import com.bernaferrari.sdkmonitor.di.AssistedViewModelFactory
import com.bernaferrari.sdkmonitor.di.DaggerMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.main.MainDataSource
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
  private val mainRepository: MainDataSource
) : MavericksViewModel<SettingsState>(state) {

  init {
    fetchData()
  }

  private fun fetchData() = withState {
    viewModelScope.async {
      settingsRepository.getSettings()
    }.execute {
      copy(data = it) }
  }

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
