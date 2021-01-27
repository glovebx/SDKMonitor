package com.bernaferrari.sdkmonitor.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.rxkprefs.Pref
import com.afollestad.rxkprefs.coroutines.asFlow
import com.afollestad.rxkprefs.rxjava.observe
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.fragmentViewModel
import com.bernaferrari.base.mvrx.simpleController
import com.bernaferrari.sdkmonitor.*
import com.bernaferrari.sdkmonitor.core.AboutDialog
import com.bernaferrari.sdkmonitor.core.AppManager
import com.bernaferrari.ui.dagger.DaggerBaseRecyclerFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class SettingsFragment : DaggerBaseRecyclerFragment() {

    private val viewModel: SettingsViewModel by fragmentViewModel()
    @Inject
    @Named(value = "lightMode") lateinit var lightMode: Pref<Boolean>
    @Inject
    @Named(value = "showSystemApps") lateinit var showSystemApps: Pref<Boolean>
    @Inject
    @Named(value = "orderBySdk") lateinit var orderBySdk: Pref<Boolean>
//    @Inject
//    @Named(value = "backgroundSync") lateinit var backgroundSync: Pref<Boolean>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
////        showSystemApps.addOnChanged {  ->  }
//        val flow = showSystemApps.asFlow()
//        launch {
//            flow.collect {
//                Log.i("showSystemApps", it.toString())
//            }
//        }

        // 在di里定义必须不能是Signelton！！！！
        showSystemApps.observe().subscribe {
            Log.i("showSystemApps", it.toString())
        }
//
//        launch {
//            withContext(NonCancellable) {
//                showSystemApps.asFlow().collect{
//                    Log.i("showSystemApps", it.toString())
//                }
//            }
//        }

//        showSystemApps.observe().subscribe {
//            Log.i("showSystemApps", it.toString())
//        }
//
//        val xx = showSystemApps.get()
//        showSystemApps.set(!xx)
//        showSystemApps.set(xx)
//        showSystemApps.set(!xx)
    }


    override fun epoxyController(): EpoxyController = simpleController(viewModel) { state ->

        println("state is: ${state.data}")
        if (state.data is Loading) {
            loadingRow { id("loading") }
        }

        if (state.data.complete) {

            marquee {
                id("header")
                title("Settings")
                subtitle("Version ${BuildConfig.VERSION_NAME}")
            }

            val isLightMode = state.data()?.lightMode ?: true

            SettingsSwitchBindingModel_()
                .id("light mode")
                .title("Light mode")
                .icon(R.drawable.ic_sunny)
                .switchIsVisible(true)
                .switchIsOn(isLightMode)
                .clickListener { v ->
//                    Injector.get().isLightTheme().set(!lightMode)
//                    viewModel.setLightTheme(!lightMode)
                    lightMode.set(!isLightMode)
                    activity?.recreate()
                }
                .addTo(this)

            val isShowSystemApps = state.data()?.showSystemApps ?: true

            SettingsSwitchBindingModel_()
                .id("system apps")
                .title("Show system apps")
                .icon(R.drawable.ic_android)
                .switchIsVisible(true)
                .switchIsOn(isShowSystemApps)
                .subtitle("Show all installed apps. This might increase loading time.")
              .clickListener { model, parentView, clickedView, position ->
//                Log.i("switchIsOn", "switchIsOn=$model.switchIsOn()")
                showSystemApps.set(!model.switchIsOn())
              }
//                .clickListener { v ->
////                    Injector.get().showSystemApps().set(!showSystemApps)
////                    AppManager.forceRefresh = true
//                    // TODO: 这里值不对！！！！
//                  showSystemApps.set(!isShowSystemApps)
//                }
                .addTo(this)

            val isOrderBySdk = state.data()?.orderBySdk ?: true

            SettingsSwitchBindingModel_()
                .id("order by")
                .title("Order by targetSDK")
                .icon(R.drawable.ic_sort)
                .subtitle("Change the order of items")
                .switchIsVisible(true)
                .switchIsOn(isOrderBySdk)
              .clickListener { model, parentView, clickedView, position ->
                orderBySdk.set(!model.switchIsOn())
              }
//                .clickListener { v ->
////                    Injector.get().orderBySdk().set(!orderBySdk)
//                  orderBySdk.set(!isOrderBySdk)
//                }
                .addTo(this)

            val backgroundSync = state.data()?.backgroundSync ?: false

            SettingsSwitchBindingModel_()
                .id("background sync")
                .title("Background Sync")
                .icon(R.drawable.ic_sync)
                .subtitle(if (backgroundSync) "Enabled" else "Disabled")
                .clickListener { v ->
                    DialogBackgroundSync.show(requireActivity())
                }
                .addTo(this)

            SettingsSwitchBindingModel_()
                .id("about")
                .title("About")
                .icon(R.drawable.ic_info)
                .clickListener { v ->
                    AboutDialog.show(requireActivity())
                }
                .addTo(this)
        }
    }
}
