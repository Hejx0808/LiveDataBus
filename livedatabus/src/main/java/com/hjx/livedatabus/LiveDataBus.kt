package com.hjx.livedatabus

/**
 * @creator HJX
 * @time 2020.02.2020/2/25
 */
class LiveDataBus private constructor() {
    companion object {
        @JvmStatic
        fun <T> getChannel(channel: Class<T>): Channel<T> {
            return Channel(channel)
        }
    }
}