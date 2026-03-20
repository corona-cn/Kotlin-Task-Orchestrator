@file:Suppress("Unused")
package io.kto.task.internal
import io.kto.task.*

sealed class TaskResult {
    /* === UNIVERSAL === */
    data class Success(
        val task: AbstractTask
    ): TaskResult()

    data class ConditionFailure(
        val task: AbstractTask
    ): TaskResult()

    data class Failure(
        val task: AbstractTask,
        val error: Throwable
    ): TaskResult()

    data class Finally(
        val task: AbstractTask
    ): TaskResult()


    /* === EVENT-DRIVEN === */
    data class Registration(
        val task: EventTask<*>
    ): TaskResult()

    data class RegistrationSuccess(
        val task: EventTask<*>
    ): TaskResult()

    data class RegistrationFailure(
        val task: EventTask<*>
    ): TaskResult()

    data class Unregistration(
        val task: EventTask<*>
    ): TaskResult()

    data class Pause(
        val task: EventTask<*>
    ): TaskResult()

    data class Resume(
        val task: EventTask<*>
    ): TaskResult()

    data class ReachedExecutionLimit(
        val task: EventTask<*>
    ): TaskResult()

    data class Cooling(
        val task: EventTask<*>
    ): TaskResult()
}