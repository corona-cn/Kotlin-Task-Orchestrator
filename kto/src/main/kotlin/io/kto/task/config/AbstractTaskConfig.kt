@file:Suppress("Unused")
package io.kto.task.config
import io.kto.task.internal.*

abstract class AbstractTaskConfig {
    /* === INTERNAL STATE === */
    /* Display Name */
    @PublishedApi
    internal var displayName: String? = null

    /* Construction */
    @PublishedApi
    internal var isConstructed: Boolean = false

    /* Callback */
    internal var callbackOnSuccess: ((TaskResult.Success) -> Unit)? = null
    internal var callbackOnConditionFailure: ((TaskResult.ConditionFailure) -> Unit)? = null
    internal var callbackOnFailure: ((TaskResult.Failure) -> Unit)? = null
    internal var callbackOnFinally: ((TaskResult.Finally) -> Unit)? = null


    /* === PUBLIC CONFIGURABLE HANDLER === */
    /* Callback */
    fun onSuccess(callback: (TaskResult.Success) -> Unit) {
        if (!isConstructed) {
            callbackOnSuccess = callback
        }
    }

    fun onConditionFailure(callback: (TaskResult.ConditionFailure) -> Unit) {
        if (!isConstructed) {
            callbackOnConditionFailure = callback
        }
    }

    fun onFailure(callback: (TaskResult.Failure) -> Unit) {
        if (!isConstructed) {
            callbackOnFailure = callback
        }
    }

    fun onFinally(callback: (TaskResult.Finally) -> Unit) {
        if (!isConstructed) {
            callbackOnFinally = callback
        }
    }
}