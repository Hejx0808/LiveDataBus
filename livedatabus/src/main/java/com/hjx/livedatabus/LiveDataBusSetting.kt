package com.hjx.livedatabus

import androidx.lifecycle.Lifecycle

/**
 * @creator HJX
 * @time 2020.02.2020/2/26
 */
class LiveDataBusSetting private constructor() {

    companion object {
        /**
         * When observer which has bind lifecycle receives the message
         */
        @JvmStatic
        var ActiveAtLast: Lifecycle.State = Lifecycle.State.CREATED

        /**
         *  Whether to remove liveData automatically LiveDataBus
         *  when there is no observer in liveData
         */
        @JvmStatic
        var AutoClean: Boolean = false
    }
}

