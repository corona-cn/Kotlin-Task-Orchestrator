@file:Suppress("Unused")
package io.kto.task
import io.kto.task.internal.*
import io.kto.event.*

import java.util.concurrent.*

import kotlinx.coroutines.*

object EventTaskDispatcher {
    /* === INTERNAL STATE === */
    /* Synchronous Task Storage */
    private val syncTaskMap = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventTask<*>>>()

    /* Asynchronous Task Storage */
    private val asyncTaskMapDefault = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventTask<*>>>()
    private val asyncTaskMapIO = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventTask<*>>>()
    private val asyncTaskMapUnconfined = ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventTask<*>>>()

    /* Task Coroutine Scope */
    private val taskScopeDefault = CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineName("Asynchronous-Default Task Executor"))
    private val taskScopeIO = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("Asynchronous-IO Task Executor"))
    private val taskScopeUnconfined = CoroutineScope(Dispatchers.Unconfined + SupervisorJob() + CoroutineName("Asynchronous-Unconfined Task Executor"))


    /* === PUBLIC HANDLER === */
    /* Event Dispatch */
    @JvmStatic
    fun dispatchEvent(event: Event) {
        dispatchAsyncEvent(event)
        dispatchSyncEvent(event)
    }

    @JvmStatic
    fun dispatchSyncEvent(event: Event) {
        val eventClass = event::class.java
        val tasks = syncTaskMap[eventClass] ?: return

        // CopyOnWriteArrayList provides snapshot iteration semantics
        for (task in tasks) {
            task.execute(event)
        }
    }

    @JvmStatic
    fun dispatchAsyncEvent(event: Event) {
        val eventClass = event::class.java

        // Execute asynchronous tasks on default dispatcher
        asyncTaskMapDefault[eventClass]?.let { tasks ->
            taskScopeDefault.launch {
                for (task in tasks) {
                    task.execute(event)
                }
            }
        }

        // Execute asynchronous tasks on IO dispatcher
        asyncTaskMapIO[eventClass]?.let { tasks ->
            taskScopeIO.launch {
                for (task in tasks) {
                    task.execute(event)
                }
            }
        }

        // Execute asynchronous tasks on unconfined dispatcher
        asyncTaskMapUnconfined[eventClass]?.let { tasks ->
            taskScopeUnconfined.launch {
                for (task in tasks) {
                    task.execute(event)
                }
            }
        }
    }

    /* Registration */
    @JvmStatic
    fun registerTask(task: EventTask<*>) {
        if (task.config.asyncSignal) {
            registerAsyncTask(task)
        } else {
            registerSyncTask(task)
        }
    }

    /* Unregistration */
    @JvmStatic
    fun unregisterTask(task: EventTask<*>): Boolean {
        val acceptedEventClass = task.config.acceptedEvent!!
        val taskMap = when (task.config.asyncDispatcher) {
            Dispatchers.Default -> asyncTaskMapDefault
            Dispatchers.IO -> asyncTaskMapIO
            else -> asyncTaskMapUnconfined
        }

        val taskList = taskMap[acceptedEventClass] ?: return false
        val removed = taskList.remove(task)

        if (removed && taskList.isEmpty()) {
            taskMap.remove(acceptedEventClass)
        }

        return removed
    }

    /* Priority Management */
    @JvmStatic
    fun changeTaskPriority(task: EventTask<*>, newPriority: Int): Boolean {
        if (task.config.asyncSignal) {
            return false
        }

        val acceptedEventClass = task.config.acceptedEvent!!
        val taskList = syncTaskMap[acceptedEventClass] ?: return false
        val removed = taskList.remove(task)

        if (removed) {
            task.config.priority = newPriority
            // Re-insert with new priority, maintaining sorted order
            val newList = CopyOnWriteArrayList<EventTask<*>>()
            newList.addAll(taskList.sortedByDescending { it.config.priority })
            newList.add(task)
            syncTaskMap[acceptedEventClass] = newList
        }

        return removed
    }


    /* === INTERNAL HANDLER === */
    /* Registration */
    private fun registerSyncTask(task: EventTask<*>) {
        val acceptedEventClass = task.config.acceptedEvent!!
        val taskList = syncTaskMap[acceptedEventClass]

        if (taskList != null) {
            // Insert while maintaining priority order
            val newList = CopyOnWriteArrayList<EventTask<*>>()
            newList.addAll(taskList)
            newList.add(task)
            syncTaskMap[acceptedEventClass] = CopyOnWriteArrayList(newList.sortedByDescending { it.config.priority })

            notifyRegistrationResult(task, syncTaskMap[acceptedEventClass]!!.contains(task))
        } else {
            val newList = CopyOnWriteArrayList<EventTask<*>>()
            newList.add(task)
            syncTaskMap[acceptedEventClass] = newList
            notifyRegistrationResult(task, true)
        }
    }

    private fun registerAsyncTask(task: EventTask<*>) {
        val acceptedEventClass = task.config.acceptedEvent!!
        val taskMap = when (task.config.asyncDispatcher) {
            Dispatchers.Default -> asyncTaskMapDefault
            Dispatchers.IO -> asyncTaskMapIO
            else -> asyncTaskMapUnconfined
        }

        val taskList = taskMap[acceptedEventClass]

        if (taskList != null) {
            taskList.add(task)
            notifyRegistrationResult(task, taskList.contains(task))
        } else {
            val newList = CopyOnWriteArrayList<EventTask<*>>()
            newList.add(task)
            taskMap[acceptedEventClass] = newList
            notifyRegistrationResult(task, true)
        }
    }

    private fun notifyRegistrationResult(task: EventTask<*>, success: Boolean) {
        if (task.config.callbackOnRegistrationSuccess != null || task.config.callbackOnRegistrationFailure != null) {
            if (success) {
                task.config.callbackOnRegistrationSuccess?.invoke(TaskResult.RegistrationSuccess(task))
            } else {
                task.config.callbackOnRegistrationFailure?.invoke(TaskResult.RegistrationFailure(task))
            }
        }
    }


    /* === PUBLIC MANAGEMENT METHOD === */
    /* Task Management */
    @JvmStatic
    fun getTaskPoolSize(): Int {
        var total = 0

        fun countFromMap(map: ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventTask<*>>>) {
            for (list in map.values) {
                total += list.size
            }
        }

        countFromMap(syncTaskMap)
        countFromMap(asyncTaskMapDefault)
        countFromMap(asyncTaskMapIO)
        countFromMap(asyncTaskMapUnconfined)

        return total
    }

    @JvmStatic
    fun getAllTasks(): List<EventTask<*>> {
        val allTasks = mutableListOf<EventTask<*>>()

        fun collectFromMap(map: ConcurrentHashMap<*, CopyOnWriteArrayList<EventTask<*>>>) {
            for (list in map.values) {
                allTasks.addAll(list)
            }
        }

        collectFromMap(syncTaskMap)
        collectFromMap(asyncTaskMapDefault)
        collectFromMap(asyncTaskMapIO)
        collectFromMap(asyncTaskMapUnconfined)

        return allTasks
    }


    /* === AUTOMATIC MANAGEMENT === */
    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            syncTaskMap.clear()
            asyncTaskMapDefault.clear()
            asyncTaskMapIO.clear()
            asyncTaskMapUnconfined.clear()

            taskScopeDefault.cancel()
            taskScopeIO.cancel()
            taskScopeUnconfined.cancel()
        })
    }
}