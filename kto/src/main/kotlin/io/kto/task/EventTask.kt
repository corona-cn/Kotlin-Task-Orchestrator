@file:Suppress("Unchecked_Cast", "Unused")
package io.kto.task
import io.kto.task.internal.*
import io.kto.task.config.*
import io.kto.event.*

class EventTask<E : Event>(
    override val config: EventTaskConfig<E>
) : AbstractTask(config) {
    /* === PUBLIC METHOD === */
    /* Registration */
    fun reg() {
        val currentState = state.get()
        if (state.compareAndSet(currentState, TaskState.Registered.Active)) {
            config.callbackOnRegistration?.invoke(TaskResult.Registration(this))
            EventTaskDispatcher.registerTask(this)
        }
    }

    fun unreg() {
        val currentState = state.get()
        if (state.compareAndSet(currentState, TaskState.Unregistered)) {
            config.callbackOnUnregistration?.invoke(TaskResult.Unregistration(this))
            EventTaskDispatcher.unregisterTask(this)
        }
    }

    /* Pause & Restart */
    fun pause() {
        val currentState = state.get()
        if (state.compareAndSet(currentState, TaskState.Registered.Paused)) {
            config.callbackOnPause?.invoke(TaskResult.Pause(this))
        }
    }

    fun resume() {
        val currentState = state.get()
        if (state.compareAndSet(currentState, TaskState.Registered.Active)) {
            config.callbackOnResume?.invoke(TaskResult.Resume(this))
        }
    }


    /* === FUNDAMENTAL EXECUTION METHOD === */
    internal fun execute(dispatchedEvent: Event) {
        // Check if the task is executable
        val currentState = state.get()
        if (!currentState.canExecute) {
            return
        }

        // Check if the dispatched event is accepted event
        val acceptedEvent = config.acceptedEvent!!
        if (acceptedEvent != dispatchedEvent::class.java || !acceptedEvent.isInstance(dispatchedEvent)) {
            return
        }

        // Check if the dispatched event can be cast to the reified accepted event
        val castEvent = dispatchedEvent as? E ?: return

        // Check if the execution limit is not been reached
        if (!checkedExecutionLimit()) {
            return
        }

        // Check if the execution condition is satisfied
        if (!checkedExecutionCondition()) {
            return
        }

        // Check if the cooling timer has passed
        if (!checkedCoolingTimer()) {
            return
        }

        // Execute the task
        try {
            config.action?.invoke(castEvent)
            config.asyncAction?.invoke(castEvent)
            config.callbackOnSuccess?.invoke(TaskResult.Success(this))
        } catch (e: Throwable) {
            config.callbackOnFailure?.invoke(TaskResult.Failure(this, e))
        } finally {
            updateExecutionCounter()
            updateCoolingMsTimer()
            config.callbackOnFinally?.invoke(TaskResult.Finally(this))
        }
    }


    /* === FUNDAMENTAL PRE-CHECKED METHOD === */
    private fun checkedExecutionLimit(): Boolean {
        val maxExecutionLimit = config.maxExecutionLimit ?: return true
        val hasReachedExecutionLimit = config.executionCounts >= maxExecutionLimit
        if (hasReachedExecutionLimit) {
            config.callbackOnReachedExecutionLimit?.invoke(TaskResult.ReachedExecutionLimit(this))
        }
        return !hasReachedExecutionLimit
    }

    private fun checkedExecutionCondition(): Boolean {
        val conditionState = config.executionCondition?.invoke() ?: return true
        if (!conditionState) {
            config.callbackOnConditionFailure?.invoke(TaskResult.ConditionFailure(this))
            if (config.resetOnConditionFail) {
                resetExecutionCounter()
                updateCoolingMsTimer()
            }
        }
        return conditionState
    }

    private fun checkedCoolingTimer(): Boolean {
        val coolingMs = config.coolingMs ?: return true
        val coolingMsTimer = config.coolingMsTimer
        val hasTimePassed = coolingMsTimer.hasTimePassed(coolingMs)
        if (!hasTimePassed) {
            config.callbackOnCooling?.invoke(TaskResult.Cooling(this))
        }
        return hasTimePassed
    }


    /* === FUNDAMENTAL MANAGEMENT METHOD === */
    /* Execution Counter */
    private fun updateExecutionCounter() {
        config.executionCounts++
    }

    private fun resetExecutionCounter() {
        config.executionCounts = 0
    }

    /* Cooling Timer */
    private fun updateCoolingMsTimer() {
        if (config.coolingMs != null) {
            config.coolingMsTimer.update()
        }
    }
}