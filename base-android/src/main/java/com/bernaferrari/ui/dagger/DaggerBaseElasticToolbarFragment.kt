package com.bernaferrari.ui.dagger

import android.content.Context
import com.bernaferrari.ui.standard.BaseElasticToolbarFragment

/**
 * BaseToolbarFragment with a Elastic behavior (user can scroll beyond top/bottom to dismiss it).
 */
abstract class DaggerBaseElasticToolbarFragment : BaseElasticToolbarFragment() {

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
