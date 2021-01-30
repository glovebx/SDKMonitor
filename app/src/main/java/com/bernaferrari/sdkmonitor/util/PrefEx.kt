package com.bernaferrari.sdkmonitor.util

import androidx.annotation.CheckResult
import com.afollestad.rxkprefs.Pref
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
@CheckResult
fun <T : Any> Pref<T>.asFlow(): Flow<T> {
    return callbackFlow {
        addOnDestroyed { close() }
        addOnChanged { offer(get()) }
        offer(get())
        awaitClose {
            destroy()
            cancel()
        }
    }
}