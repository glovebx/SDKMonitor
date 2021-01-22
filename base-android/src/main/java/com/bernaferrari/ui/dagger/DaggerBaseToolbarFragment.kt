package com.bernaferrari.ui.dagger

import android.content.Context
import com.bernaferrari.ui.standard.BaseToolbarFragment

/**
 * Simple fragment with a toolbar and a recyclerview.
 */
abstract class DaggerBaseToolbarFragment : BaseToolbarFragment() {

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
