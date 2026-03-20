@file:Suppress("Unused")
package io.kto.event

open class CancellableEvent : Event {
    open var isCancelled: Boolean = false
    open fun cancel() {
        isCancelled = true
    }
}