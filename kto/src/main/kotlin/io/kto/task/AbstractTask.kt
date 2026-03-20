@file:Suppress("Unused")
package io.kto.task
import io.kto.task.internal.*
import io.kto.task.config.*

import java.util.concurrent.atomic.*
import java.util.*

abstract class AbstractTask internal constructor(
    internal open val config: AbstractTaskConfig
) {
    /* === IDENTITY === */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || this::class != other::class) {
            return false
        }

        return uuid == (other as? AbstractTask)?.uuid
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    override fun toString(): String {
        return "Task(uuid=$uuid, displayName=${config.displayName}, state=$state)"
    }


    /* === BASE STATE === */
    /* Identity */
    internal val uuid: UUID by lazy { UUID.randomUUID() }

    /* State */
    internal var state: AtomicReference<TaskState> = AtomicReference(TaskState.Idle)
}