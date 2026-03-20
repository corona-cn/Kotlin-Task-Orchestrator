@file:Suppress("Unused")
package io.kto.util

class MSTimer {
    /* === INTERNAL STATE === */
    private var time: Long = System.currentTimeMillis()


    /* === EXTERNAL OPERATION === */
    fun getCurrentTime(): Long = time

    fun getPassedTime(): Long = System.currentTimeMillis() - time

    fun update() {
        time = System.currentTimeMillis()
    }

    fun hasTimePassed(ms: Long): Boolean = time + ms < System.currentTimeMillis()
}