@file:Suppress("Unused")
package io.kto.task.config
import io.kto.task.internal.*
import io.kto.event.*
import io.kto.util.*

import kotlinx.coroutines.*

class EventTaskConfig<E : Event> : AbstractTaskConfig() {
    /* === INTERNAL CONFIGURATION === */
    /* Priority */
    internal var priority: Int = 0

    /* Execution Limit */
    internal var maxExecutionLimit: Int? = null

    /* Execution Condition */
    internal var resetOnConditionFail: Boolean = false

    /* Cooling Timer */
    internal var coolingMs: Long? = null


    /* === INTERNAL STATE === */
    /* Accepted Event */
    @PublishedApi
    internal var acceptedEvent: Class<E>? = null

    /* Action */
    @PublishedApi
    internal var action: ((E) -> Unit)? = null

    @PublishedApi
    internal var asyncAction: ((E) -> Unit)? = null

    /* Asynchrony Signal */
    internal var asyncSignal: Boolean = false
    internal var asyncDispatcher: CoroutineDispatcher = Dispatchers.Default

    /* Callback */
    internal var callbackOnRegistrationSuccess: ((TaskResult.RegistrationSuccess) -> Unit)? = null
    internal var callbackOnRegistrationFailure: ((TaskResult.RegistrationFailure) -> Unit)? = null
    internal var callbackOnRegistration: ((TaskResult.Registration) -> Unit)? = null
    internal var callbackOnUnregistration: ((TaskResult.Unregistration) -> Unit)? = null
    internal var callbackOnPause: ((TaskResult.Pause) -> Unit)? = null
    internal var callbackOnResume: ((TaskResult.Resume) -> Unit)? = null
    internal var callbackOnReachedExecutionLimit: ((TaskResult.ReachedExecutionLimit) -> Unit)? = null
    internal var callbackOnCooling: ((TaskResult.Cooling) -> Unit)? = null


    /* === CONFIGURATION STATE === */
    /* Execution Condition */
    internal var executionCondition: (() -> Boolean)? = null

    /* Execution Limit */
    internal var executionCounts = 0

    /* Cooling Timer */
    internal var coolingMsTimer: MSTimer = MSTimer()


    /* === PUBLIC CONFIGURABLE HANDLER === */
    /* Priority */
    fun setPriority(priority: Int) {
        if (!isConstructed) {
            this.priority = priority
        }
    }

    /* Execution Condition */
    fun condition(resetOnConditionFail: Boolean = false, condition: () -> Boolean) {
        if (!isConstructed) {
            this.resetOnConditionFail = resetOnConditionFail
            executionCondition = condition
        }
    }

    /* Execution Limit */
    fun limitExecution(maxExecutionLimit: Int) {
        if (!isConstructed) {
            this.maxExecutionLimit = maxExecutionLimit
        }
    }

    /* Cooling */
    fun cooling(coolingMs: Long) {
        if (!isConstructed) {
            this.coolingMs = coolingMs
        }
    }

    /* Execution */
    fun handle(action: (E) -> Unit) {
        if (!isConstructed) {
            this.action = action
        }
    }

    fun handleAsync(dispatchers: CoroutineDispatcher = Dispatchers.Default, action: (E) -> Unit) {
        if (!isConstructed) {
            this.asyncSignal = true
            this.asyncDispatcher = dispatchers
            this.asyncAction = action
        }
    }

    /* Callback */
    fun onRegistration(callback: (TaskResult.Registration) -> Unit) {
        if (!isConstructed) {
            this.callbackOnRegistration = callback
        }
    }

    fun onRegistrationSuccess(callback: (TaskResult.RegistrationSuccess) -> Unit) {
        if (!isConstructed) {
            this.callbackOnRegistrationSuccess = callback
        }
    }

    fun onRegistrationFailure(callback: (TaskResult.RegistrationFailure) -> Unit) {
        if (!isConstructed) {
            this.callbackOnRegistrationFailure = callback
        }
    }

    fun onUnregistration(callback: (TaskResult.Unregistration) -> Unit) {
        if (!isConstructed) {
            this.callbackOnUnregistration = callback
        }
    }

    fun onPause(callback: (TaskResult.Pause) -> Unit) {
        if (!isConstructed) {
            this.callbackOnPause = callback
        }
    }

    fun onResume(callback: (TaskResult.Resume) -> Unit) {
        if (!isConstructed) {
            this.callbackOnResume = callback
        }
    }

    fun onReachedExecutionLimit(callback: (TaskResult.ReachedExecutionLimit) -> Unit) {
        if (!isConstructed) {
            this.callbackOnReachedExecutionLimit = callback
        }
    }

    fun onCooling(callback: (TaskResult.Cooling) -> Unit) {
        if (!isConstructed) {
            this.callbackOnCooling = callback
        }
    }


    /* === PUBLIC METHOD === */
    /* Execution Counter */
    fun getExecutionCounts(): Int {
        return executionCounts
    }

    fun resetExecutionCounter() {
        executionCounts = 0
    }
}