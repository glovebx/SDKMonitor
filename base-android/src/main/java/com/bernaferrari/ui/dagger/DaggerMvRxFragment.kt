package com.bernaferrari.ui.dagger

import android.content.Context
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView


abstract class DaggerMvRxFragment : Fragment(), MavericksView {

//    open val shouldInject: Boolean = true

//    @Inject
//    lateinit var androidInjector: DispatchingAndroidInjector<Any>

    override fun onAttach(context: Context) {
//        if (shouldInject) {
//            AndroidSupportInjection.inject(this)
//        }
        super.onAttach(context)
    }

//    override fun androidInjector(): AndroidInjector<Any>? {
//        return androidInjector
//    }
}
