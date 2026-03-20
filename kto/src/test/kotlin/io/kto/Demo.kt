@file:Suppress("Unused")
package io.kto
import io.kto.event.*
import io.kto.task.*

object Demo {
    @JvmStatic
    fun main(args: Array<String>) {
        log("Starting Demo")

        Thread {
            while (true) {
                EventTaskDispatcher.dispatchEvent(EventDemo())
                Thread.sleep(1000L)
            }
        }.start()

        regEventTask("Demo") {
            limitExecution(10)
            handle { event : EventDemo ->
                log("EventDemo: $event")
            }

            onReachedExecutionLimit { result ->
                log("onReachedExecutionLimit: $result")
            }
        }

        Thread.currentThread().join()
    }
}

class EventDemo : Event