package com.bernaferrari.sdkmonitor.settings

import android.util.Log
import com.airbnb.mvrx.*
import com.bernaferrari.sdkmonitor.di.hiltMavericksViewModelFactory
import com.bernaferrari.sdkmonitor.util.asFlow
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
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
        @Assisted val state: SettingsState,
        @Assisted val sources: Flow<SettingsData>
//  private val settingsRepository: SettingsRepository,
//  private val mainRepository: MainDataSource,
//  @Named(value = "lightMode") val lightMode: Pref<Boolean>,
//  @Named(value = "showSystemApps") val showSystemApps: Pref<Boolean>,
//  @Named(value = "backgroundSync") val backgroundSync: Pref<Boolean>,
//  @Named(value = "orderBySdk") val orderBySdk: Pref<Boolean>
) : MavericksViewModel<SettingsState>(state) {

  lateinit var job: Job
  init {
    job = fetchData()
  }

  inline fun <reified T> instantCombine(vararg flows: Flow<T>) = channelFlow {
    val array= Array(flows.size) {
      false to (null as T) // first element stands for "present"
    }

    flows.forEachIndexed { index, flow ->
      launch {
        flow.collect { emittedElement ->
          array[index] = true to emittedElement
          send(array.filter { it.first }.map { it.second })
        }
      }
    }
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
//          viewModelScope.launch(SupervisorJob()) {
            sources.execute { copy(data = it) }
//          }.let {
//            println("${it}")
//          }
//
//    val handler = CoroutineExceptionHandler {
//      context, exception -> println("Caught $exception")
//    }
//    viewModelScope.launch(SupervisorJob()) {
////      flow {
////        emit(SettingsData(lightMode.get(), showSystemApps.get(), backgroundSync.get(), orderBySdk.get()))
////      }.execute {
////        copy(data = it)
////      }
//
//      // kotlinx.coroutines.JobCancellationException: ProducerCoroutine was cancelled; job=ProducerCoroutine{Cancelled}@8482992
////        combine(
////          lightMode.asFlow().catch { emit(false) },
////          showSystemApps.asFlow().catch { emit(false) },
////          backgroundSync.asFlow().catch { emit(false) },
////          orderBySdk.asFlow().catch { emit(false) }
////        ) { dark, system, backgroundSync, orderBySdk ->
////          SettingsData(dark, system, backgroundSync, orderBySdk)
////        }.catch {
////          Log.i("viewModelScope====", it.message ?: "error")
////        }.execute { copy(data = it) }
////
////      // 只要用上combine，则必然出 JobCancellationException 错
////        showSystemApps.asFlow().combine(orderBySdk.asFlow()) {
////          system, orderBy ->
////            SettingsData(true, system, true, orderBy)
////        }.execute { copy(data = it) }
////
////      showSystemApps.asFlow().collect {
////        flow {
////          emit(SettingsData(lightMode.get(), it, backgroundSync.get(), orderBySdk.get()))
////        }.execute {
////          copy(data = it)
////        }
////      }
//
////
////      // 如果上面的collect被执行，则程序不会到此处！！
////      orderBySdk.asFlow().cancellable().collect {
//////        if (isActive) {
////          flow {
////            emit(SettingsData(lightMode.get(), showSystemApps.get(), backgroundSync.get(), it))
////          }.execute {
////              copy(data = it)
////          }
//////        }
////      }
//    }


//    viewModelScope.launch {
//      withContext(Dispatchers.IO) {
//        instantCombine(
//          lightMode.asFlow(),
//          showSystemApps.asFlow(),
//          backgroundSync.asFlow(),
//          orderBySdk.asFlow()
//        ).collect { it ->
//          if (it.size == 4) {
//            flow {
//              emit(SettingsData(it[0], it[1], it[2], it[3]))
//            }.execute {
//              copy(data = it)
//            }
//          }
//        }
//      }
//    }

//    viewModelScope.launch {
//      withContext(Dispatchers.IO) {
//        showSystemApps.asFlow().execute {
//
//        }
//        flow {
//          var system = showSystemApps.asFlow().first()
//          emit(SettingsData(true, system, true, true))
//        }.execute {
//          copy(data = it)
//        }
//      }
//    }

//
//    viewModelScope.launch {
//      withContext(Dispatchers.IO) {
//        showSystemApps.asFlow().collect{
//          system -> flow {
//          emit(          SettingsData(true, system, true, true))
//        }.execute {
//          copy(data = it) }
//        }
//      }
//    }

//    // 这个逻辑可用
//    // 手机锁屏黑屏几秒钟后，这里就不再响应了！！！
//      Observables.combineLatest(
//              lightMode.observe(),
//              showSystemApps.observe(),
//              backgroundSync.observe(),
//              orderBySdk.observe()
//      ) { dark, system, backgroundSync, orderBySdk ->
//        SettingsData(dark, system, backgroundSync, orderBySdk)
//      }.subscribe {
//        setState {
//          copy(data = Success(it)) }
//      }


//    viewModelScope.launch {
//      showSystemApps.asFlow().collect {
//                Log.i("showSystemApps","showSystemApps=$it")
//      }
//    }

//
//  fun setLightTheme(isLightMode: Boolean) {
//    settingsRepository.toggleLightTheme(isLightMode)
//  }
//
//  fun setShowSystemApps(isShowSystemApps: Boolean) {
//    settingsRepository.toggleShowSystemApps(isShowSystemApps)
//    mainRepository.forceRefresh = true
//  }
//
//  fun setOrderBySdk(isOrderBySdk: Boolean) {
//    settingsRepository.toggleOrderBySdk(isOrderBySdk)
//  }

  @AssistedInject.Factory
  interface Factory {
    fun create(state: SettingsState,
               sources: Flow<SettingsData>): SettingsViewModel
  }

  companion object : MavericksViewModelFactory<SettingsViewModel, SettingsState> by hiltMavericksViewModelFactory() {
//    @Named(value = "lightMode") lateinit var lightMode: Pref<Boolean>
//    @Named(value = "showSystemApps") lateinit var showSystemApps: Pref<Boolean>
//    @Named(value = "backgroundSync") lateinit var backgroundSync: Pref<Boolean>
//    @Named(value = "orderBySdk") lateinit var orderBySdk: Pref<Boolean>

    override fun create(viewModelContext: ViewModelContext, state: SettingsState): SettingsViewModel? {
      val fragment: SettingsFragment =
              (viewModelContext as FragmentViewModelContext).fragment()

        val sources = combine(
                fragment.lightMode.asFlow(),
                fragment.showSystemApps.asFlow(),
                fragment.backgroundSync.asFlow(),
                fragment.orderBySdk.asFlow()
        ) { dark, system, backgroundSync, orderBySdk ->
          SettingsData(dark, system, backgroundSync, orderBySdk)
        }

      return fragment.settingsViewModelFactory.create(state, sources)
    }
  }

  override fun onCleared() {
    Log.i("SettingsViewModel", "onCleared*********************")
    super.onCleared()
  }
}
