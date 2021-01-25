package com.bernaferrari.sdkmonitor.data

import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.bernaferrari.sdkmonitor.settings.SettingsData
import kotlinx.coroutines.flow.*
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

    fun getSettings(): Flow<SettingsData> {
        return combine(lightMode.asFlow()
                , showSystemApps.asFlow()
                , backgroundSync.asFlow()
                , orderBySdk.asFlow()) {
            b: Boolean, b1: Boolean, b2: Boolean, b3: Boolean ->
            SettingsData(b, b1, b2, b3)
        }
    }

    fun toggleLightTheme(isLightMode: Boolean) {
        lightMode.set(isLightMode)
    }

}
