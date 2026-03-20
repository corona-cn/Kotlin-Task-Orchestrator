@file:Suppress("Unused")
package io.kto
import io.kto.task.config.*
import io.kto.event.*
import io.kto.task.*

/* === EVENT-DRIVEN TASK BUILDER === */
/* Build */
inline fun <reified E: Event> buildEventTask(displayName: String? = null, builder: EventTaskConfig<E>.() -> Unit): EventTask<E> {
    val config = EventTaskConfig<E>().apply {
        this.acceptedEvent = E::class.java
        this.apply(builder)
        this.displayName = displayName
        this.isConstructed = true
    }

    require(config.action != null || config.asyncAction != null) {
        "At least one action must be configured (handle or handleAsync)"
    }

    require(config.acceptedEvent != null) {
        "Accepted event type must be specified"
    }

    return EventTask(config)
}

/* Build & Register */
inline fun <reified E: Event> regEventTask(displayName: String? = null, builder: EventTaskConfig<E>.() -> Unit): EventTask<E> {
    return buildEventTask(displayName = displayName, builder = builder).apply { this.reg() }
}