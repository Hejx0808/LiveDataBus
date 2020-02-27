package com.hjx.livedatabus

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hjx.livedatabus.core.Core

/**
 * @creator HJX
 * @time 2020.02.2020/2/26
 */
@Suppress("MemberVisibilityCanBePrivate")
open class Channel<T> constructor(type: Class<T>) {

    // MainHandler
    internal val mMainHandler: Handler = Handler(Looper.getMainLooper())

    // ChannelName
    internal val mChannel = type.simpleName

    @MainThread
    open fun observeLifecycle(owner: LifecycleOwner, observer: Observer<T>) {
        Core.observe(mChannel, owner, observer)
    }

    @MainThread
    open fun observeLifecycleSticky(owner: LifecycleOwner, observer: Observer<T>) {
        Core.observeSticky(mChannel, owner, observer)
    }

    open fun observe(observer: Observer<T>) {
        if (isMainThread()) {
            Core.observeForever(mChannel, observer)
        } else {
            mMainHandler.post {
                Core.observeForever(mChannel, observer)
            }
        }
    }

    open fun observeSticky(observer: Observer<T>) {
        if (isMainThread()) {
            Core.observeStickyForever(mChannel, observer)
        } else {
            mMainHandler.post {
                Core.observeStickyForever(mChannel, observer)
            }
        }
    }

    open fun removeObserve(observer: Observer<T>) {
        if (isMainThread()) {
            Core.removeObserve(mChannel, observer)
        } else {
            mMainHandler.post {
                Core.removeObserve(mChannel, observer)
            }
        }
    }

    open fun postEvent(event: T) {
        if (isMainThread()) {
            Core.postEvent(mChannel, event)
        } else {
            mMainHandler.post {
                Core.postEvent(mChannel, event)
            }
        }
    }

    open fun postEventDelay(event: T, delay: Long) {
        mMainHandler.postDelayed({
            Core.postEvent(mChannel, event)
        }, delay)
    }

    fun isMainThread() =
        Looper.myLooper() == Looper.getMainLooper()
}