@file:Suppress("Unused")
package io.kto.task.internal

internal sealed class TaskState {
    /* === CORE STATE MODEL === */
    object Idle : TaskState()
    sealed class Registered : TaskState() {
        object Active : Registered()
        object Paused : Registered()
    }
    object Unregistered : TaskState()


    /* === STATE CHECK === */
    /* Direct */
    val isIdle: Boolean get() = this is Idle
    val isRegistered: Boolean get() = this is Registered
    val isActive: Boolean get() = this is Registered.Active
    val isPaused: Boolean get() = this is Registered.Paused
    val isUnregistered: Boolean get() = this is Unregistered

    /* Extended */
    val canRegister: Boolean get() = isIdle || isUnregistered
    val canUnregister: Boolean get() = isRegistered
    val canPause: Boolean get() = isActive
    val canResume: Boolean get() = isPaused
    val canExecute: Boolean get() = isActive
}