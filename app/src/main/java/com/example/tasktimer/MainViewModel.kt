package com.example.tasktimer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_TASKS = "key_tasks"
        private const val KEY_ACTIVE_ID = "key_active_id"
        private const val KEY_ACCUMULATED_MILLIS = "key_accumulated_millis"
        private const val KEY_START_TIMESTAMP = "key_start_timestamp"
        private const val KEY_IS_RUNNING = "key_is_running"
        private const val KEY_NEXT_ID = "key_next_id"
    }

    private val _tasks = MutableStateFlow<List<Task>>(
        savedStateHandle.get<ArrayList<Task>>(KEY_TASKS) ?: arrayListOf()
    )
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _activeTaskId = MutableStateFlow(savedStateHandle.get<Long>(KEY_ACTIVE_ID))
    val activeTaskId: StateFlow<Long?> = _activeTaskId.asStateFlow()

    val activeTask: Task?
        get() = _activeTaskId.value?.let { id -> _tasks.value.find { it.id == id } }

    private var accumulatedMillis: Long =
        savedStateHandle.get<Long>(KEY_ACCUMULATED_MILLIS) ?: 0L
    private var startTimestamp: Long =
        savedStateHandle.get<Long>(KEY_START_TIMESTAMP) ?: 0L
    private var isRunning: Boolean =
        savedStateHandle.get<Boolean>(KEY_IS_RUNNING) ?: false

    private var nextId: Long = savedStateHandle.get<Long>(KEY_NEXT_ID) ?: 1L

    private val _elapsedMillis = MutableStateFlow(calculateElapsedMillis())
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    val isTimerRunning: Boolean
        get() = isRunning

    private var tickerJob: Job? = null

    private fun calculateElapsedMillis(): Long {
        return if (isRunning) {
            accumulatedMillis + (System.currentTimeMillis() - startTimestamp)
        } else {
            accumulatedMillis
        }
    }

    private fun refreshElapsed() {
        _elapsedMillis.value = calculateElapsedMillis()
    }

    private fun persistTimerState() {
        savedStateHandle[KEY_ACCUMULATED_MILLIS] = accumulatedMillis
        savedStateHandle[KEY_START_TIMESTAMP] = startTimestamp
        savedStateHandle[KEY_IS_RUNNING] = isRunning
    }

    private fun persistTasks() {
        savedStateHandle[KEY_TASKS] = ArrayList(_tasks.value)
        savedStateHandle[KEY_NEXT_ID] = nextId
    }

    private fun persistActiveId() {
        savedStateHandle[KEY_ACTIVE_ID] = _activeTaskId.value
    }

    fun startTicker() {
        if (tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (true) {
                refreshElapsed()
                delay(500)
            }
        }
    }

    fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun addTask(name: String) {
        if (name.isBlank()) return
        val newTask = Task(id = nextId, name = name.trim())
        nextId += 1
        _tasks.value = _tasks.value + newTask
        persistTasks()
    }

    fun startTask(taskId: Long) {
        // Si había otra tarea corriendo, la pausamos y guardamos su progreso.
        if (isRunning && _activeTaskId.value != taskId) {
            pauseActiveTask()
        }

        val wasAlreadyActive = _activeTaskId.value == taskId
        _activeTaskId.value = taskId
        persistActiveId()

        val current = _tasks.value.find { it.id == taskId } ?: return
        accumulatedMillis = if (wasAlreadyActive) accumulatedMillis else current.elapsedMillis
        startTimestamp = System.currentTimeMillis()
        isRunning = true
        persistTimerState()
        refreshElapsed()
    }

    fun pauseActiveTask() {
        if (!isRunning) return
        val id = _activeTaskId.value ?: return

        accumulatedMillis = calculateElapsedMillis()
        isRunning = false
        persistTimerState()
        refreshElapsed()

        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(elapsedMillis = accumulatedMillis) else task
        }
        persistTasks()
    }

    /** Marca la tarea activa como completada y la envía al historial. */
    fun completeActiveTask() {
        val id = _activeTaskId.value ?: return
        val finalElapsed = calculateElapsedMillis()

        _tasks.value = _tasks.value.map { task ->
            if (task.id == id) task.copy(elapsedMillis = finalElapsed, isCompleted = true) else task
        }
        persistTasks()

        isRunning = false
        accumulatedMillis = 0L
        startTimestamp = 0L
        _activeTaskId.value = null
        persistTimerState()
        persistActiveId()
        refreshElapsed()
    }

    fun onAppBackgrounded() {
        persistTimerState()
        persistTasks()
        persistActiveId()
    }

    fun onAppForegrounded() {
        refreshElapsed()
    }

    override fun onCleared() {
        super.onCleared()
        stopTicker()
    }
}
