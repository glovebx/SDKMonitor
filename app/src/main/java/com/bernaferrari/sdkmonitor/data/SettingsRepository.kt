package com.bernaferrari.sdkmonitor.data

import android.util.Log
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.bernaferrari.sdkmonitor.settings.SettingsData
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

//                Injector.get().isLightTheme().observe(),
//                Injector.get().showSystemApps().observe(),
//                Injector.get().backgroundSync().observe(),
//                Injector.get().orderBySdk().observe()
class SettingsRepository @Inject constructor(
  @Named(value = "lightMode") val lightMode: Pref<Boolean>,
  @Named(value = "showSystemApps") val showSystemApps: Pref<Boolean>,
  @Named(value = "backgroundSync") val backgroundSync: Pref<Boolean>,
  @Named(value = "orderBySdk") val orderBySdk: Pref<Boolean>
) {

  suspend fun getSettings(): SettingsData = withContext(Dispatchers.Default) {
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
    combine(
      lightMode.asFlow(),
      showSystemApps.asFlow(),
      backgroundSync.asFlow(),
      orderBySdk.asFlow()
    ) { dark, system, backgroundSync, orderBySdk ->
      SettingsData(dark, system, backgroundSync, orderBySdk)
    }.first()
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
