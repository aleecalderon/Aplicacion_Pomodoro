package com.example.tasktimer

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tasktimer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LIFECYCLE_DEMO"
        private const val KEY_SCROLL_DEMO = "key_scroll_demo" // solo para demostrar onSaveInstanceState
    }

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    private lateinit var pendingAdapter: TaskAdapter
    private lateinit var historyAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "onCreate() -> UI creada. ViewModel activo: $viewModel")

        val restoredScroll = savedInstanceState?.getInt(KEY_SCROLL_DEMO, 0) ?: 0
        Log.d(TAG, "onCreate() -> valor de ejemplo restaurado desde Bundle: $restoredScroll")

        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        pendingAdapter = TaskAdapter { task -> viewModel.startTask(task.id) }
        historyAdapter = TaskAdapter()

        binding.rvPendingTasks.layoutManager = LinearLayoutManager(this)
        binding.rvPendingTasks.adapter = pendingAdapter

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = historyAdapter
    }

    private fun setupListeners() {
        binding.btnAddTask.setOnClickListener {
            val name = binding.etTaskName.text?.toString().orEmpty()
            viewModel.addTask(name)
            binding.etTaskName.text?.clear()
        }

        binding.btnStartPause.setOnClickListener {
            val active = viewModel.activeTask
            when {
                active == null -> { /* No hay tarea seleccionada; el usuario debe tocar una de la lista */ }
                viewModel.isTimerRunning -> viewModel.pauseActiveTask()
                else -> viewModel.startTask(active.id)
            }
        }

        binding.btnComplete.setOnClickListener {
            viewModel.completeActiveTask()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasks.collect { tasks ->
                        pendingAdapter.submitList(tasks.filter { !it.isCompleted })
                        historyAdapter.submitList(tasks.filter { it.isCompleted })
                    }
                }
                launch {
                    viewModel.activeTaskId.collect { activeId ->
                        val task = viewModel.tasks.value.find { it.id == activeId }
                        binding.tvActiveTaskLabel.text =
                            task?.name ?: getString(R.string.label_no_active_task)
                        binding.btnStartPause.text =
                            if (viewModel.isTimerRunning) getString(R.string.btn_pause)
                            else getString(R.string.btn_start)
                    }
                }
                launch {
                    viewModel.elapsedMillis.collect { millis ->
                        binding.tvTimer.text = formatMillis(millis)
                        binding.btnStartPause.text =
                            if (viewModel.isTimerRunning) getString(R.string.btn_pause)
                            else getString(R.string.btn_start)
                    }
                }
            }
        }
    }

    private fun formatMillis(millis: Long): String {
        val h = TimeUnit.MILLISECONDS.toHours(millis)
        val m = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val s = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() -> app visible de nuevo, reanudando ticker y recalculando tiempo real")
        viewModel.onAppForegrounded()
        viewModel.startTicker()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() -> app en segundo plano, deteniendo ticker de UI y guardando estado")
        viewModel.stopTicker()
        viewModel.onAppBackgrounded()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val scrollPosition = (binding.rvPendingTasks.layoutManager as? LinearLayoutManager)
            ?.findFirstVisibleItemPosition() ?: 0
        outState.putInt(KEY_SCROLL_DEMO, scrollPosition)
        Log.d(TAG, "onSaveInstanceState() -> guardando estado de UI de respaldo (scroll=$scrollPosition)")
    }
}
