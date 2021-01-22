package com.bernaferrari.sdkmonitor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.afollestad.rxkprefs.Pref
import com.airbnb.mvrx.MavericksView
import com.bernaferrari.sdkmonitor.data.source.local.AppsDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    @Named(value = "lightMode") lateinit var isLightTheme: Pref<Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 必须放在 super.onCreate 之后，否则未注入！！
        if (isLightTheme.get()) {
            setTheme(R.style.AppThemeLight)
        } else {
            setTheme(R.style.AppThemeDark)
        }
//        if (Injector.get().isLightTheme().get()) {
//            setTheme(R.style.AppThemeLight)
//        } else {
//            setTheme(R.style.AppThemeDark)
//        }

        setContentView(R.layout.activity_main)

        NavigationUI.setupWithNavController(
            bottom_nav,
            nav_host_fragment.findNavController()
        )
    }

}
