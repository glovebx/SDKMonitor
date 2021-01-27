package com.bernaferrari.sdkmonitor.data

import android.util.Log
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.bernaferrari.sdkmonitor.settings.SettingsData
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

//                Injector.get().isLightTheme().observe(),
//                Injector.get().showSystemApps().observe(),
//                Injector.get().backgroundSync().observe(),
//                Injector.get().orderBySdk().observe()
@Singleton
class SettingsRepository @Inject constructor(
  @Named(value = "lightMode") val lightMode: Pref<Boolean>,
  @Named(value = "showSystemApps") val showSystemApps: Pref<Boolean>,
  @Named(value = "backgroundSync") val backgroundSync: Pref<Boolean>,
  @Named(value = "orderBySdk") val orderBySdk: Pref<Boolean>
) {

  fun getSettings(): Flow<SettingsData> =
//    lightMode.asFlow().collect {
//      Log.i("lightMode", "lightMode=$it")
//    }
//    showSystemApps.asFlow().collect {
//      Log.i("showSystemApps", "showSystemApps=$it")
//    }
//    backgroundSync.asFlow().collect {
//      Log.i("backgroundSync", "backgroundSync=$it")
//    }
//    orderBySdk.asFlow().collect {
//      Log.i("orderBySdk", "orderBySdk=$it")
//    }
//          lightMode.asFlow().combine(showSystemApps.asFlow()) {
//            dark, system ->
//            SettingsData(dark, system, true, true)
//          }
              combine(
                      lightMode.asFlow().conflate(),
                      showSystemApps.asFlow().conflate(),
                      backgroundSync.asFlow().conflate(),
                      orderBySdk.asFlow().conflate()
              ) { dark, system, backgroundSync, orderBySdk ->
                  SettingsData(dark, system, backgroundSync, orderBySdk)
              }


  fun toggleLightTheme(isLightMode: Boolean) {
    lightMode.set(isLightMode)
  }

  fun toggleShowSystemApps(isShowSystemApps: Boolean) {
    showSystemApps.set(isShowSystemApps)
  }

  fun toggleOrderBySdk(isOrderBySdk: Boolean) {
    orderBySdk.set(isOrderBySdk)
  }
}
