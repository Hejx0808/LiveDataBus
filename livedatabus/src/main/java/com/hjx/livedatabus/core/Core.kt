package com.hjx.livedatabus.core

import androidx.annotation.MainThread
import androidx.lifecycle.BusLiveData
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.hjx.livedatabus.LiveDataBusSetting
import com.hjx.livedatabus.LiveDataBusSetting.Companion.ActiveAtLast


/**
 * @creator HJX
 * @time 2020.02.2020/2/26
 */
@Suppress("UNCHECKED_CAST")
object Core {

    private val mBus: MutableMap<String, BusLiveData<*>> = hashMapOf()
    private val mObservers: MutableMap<Observer<*>, ObserverWrapper<*>> = hashMapOf()

    @MainThread
    fun <T> observe(channel: String, owner: LifecycleOwner, observer: Observer<T>) {
        val liveData = getChannel<T>(channel)
        val skip = liveData.version > BusLiveData.START_VERSION
        liveData.observe(owner, mObservers.getOrPut(observer) {
            ObserverWrapper(observer, skip)
        } as Observer<T>)
    }

    @MainThread
    fun <T> observeSticky(channel: String, owner: LifecycleOwner, observer: Observer<T>) {
        val liveData = getChannel<T>(channel)
        liveData.observe(owner, mObservers.getOrPut(observer) {
            ObserverWrapper(observer)
        } as Observer<T>)
    }

    @MainThread
    fun <T> observeForever(channel: String, observer: Observer<T>) {
        val liveData = getChannel<T>(channel)
        val skip = liveData.version > BusLiveData.START_VERSION
        liveData.observeForever(mObservers.getOrPut(observer) {
            ObserverWrapper(observer, skip)
        } as Observer<T>)
    }

    @MainThread
    fun <T> observeStickyForever(channel: String, observer: Observer<T>) {
        val liveData = getChannel<T>(channel)
        liveData.observeForever(mObservers.getOrPut(observer) {
            ObserverWrapper(observer)
        } as Observer<T>)
    }

    @MainThread
    fun <T> removeObserve(channel: String, observer: Observer<T>) {
        // get real observer
        val realObserver = if (mObservers.containsKey(observer)) {
            mObservers.remove(observer) as Observer<T>
        } else {
            observer
        }

        if (!mBus.containsKey(channel)) {
            return
        }
        // remove
        val liveData = mBus[channel] as BusLiveData<T>
        liveData.removeObserver(realObserver)

        // Automatically remove liveData when there is no observer in liveData
        if (LiveDataBusSetting.AutoClean && !liveData.hasObservers()) {
            mBus.remove(channel)
        }
    }

    @MainThread
    fun <T> postEvent(channel: String, event: T) {
        getChannel<T>(channel).value = event
    }

    private fun <T> getChannel(channelName: String): BusLiveData<T> =
        mBus.getOrPut(channelName) { MyBusLiveData<T>() } as BusLiveData<T>


    /**
     * control when the observer active by setting
     */
    internal class MyBusLiveData<T> : BusLiveData<T>() {
        override fun whenObserverShouldBeActive(): Lifecycle.State = ActiveAtLast
    }

    /**
     * ObserverWrapper
     * be used for control StickyEvent
     */
    internal class ObserverWrapper<T> constructor(
        private val mObserver: Observer<T>,
        private var mSkipStickyEvent: Boolean = false
    ) : Observer<T> {

        override fun onChanged(t: T) {
            // skip StickyEvent
            if (mSkipStickyEvent) {
                mSkipStickyEvent = false
                return
            }
            // normal process
            mObserver.onChanged(t)
        }
    }
}