package com.example.investigacion1

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.investigacion1.databinding.ActivityMainBinding
import com.example.investigacion1.databinding.ItemTaskBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Variables para el temporizador Pomodoro
    private var countDownTimer: CountDownTimer? = null
    private val START_TIME_IN_MILLIS: Long = 25 * 60 * 1000 // 25 minutos en milisegundos
    private var timeLeftInMillis: Long = START_TIME_IN_MILLIS
    private var timerRunning: Boolean = false

    // Contadores para el resumen
    private var tareasPendientes = 0
    private var sesionesCompletadas = 0

    private var totalTareas = 0

    private var activeTask: TextView? = null
    private var activeTaskName: String = "Sin tarea seleccionada"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPause.isEnabled = false
        binding.btnResume.isEnabled = false
        binding.btnReset.isEnabled = false

        // 1. LÓGICA DE LOS BOTONES DEL TEMPORIZADOR
        binding.btnStart.setOnClickListener {
            if (!timerRunning) startTimer()
        }

        binding.btnPause.setOnClickListener {
            if (timerRunning) pauseTimer()
        }

        binding.btnResume.setOnClickListener {
            if (!timerRunning) startTimer()
        }

        binding.btnReset.setOnClickListener {
            resetTimer()
        }

        // 2. LÓGICA DEL BOTÓN "AGREGAR TAREA"
        binding.btnAddTask.setOnClickListener {

            val taskText = binding.inputTask.text.toString().trim()

            if (taskText.isEmpty()) {
                Toast.makeText(
                    this,
                    "Por favor escribe una tarea",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Verifica si ya existe una tarea con ese nombre
            for (i in 0 until binding.containerTasks.childCount) {

                val taskView = binding.containerTasks.getChildAt(i)

                val title =
                    taskView.findViewById<TextView>(R.id.tv_task_title)

                if (title.text.toString().equals(taskText, true)) {

                    Toast.makeText(
                        this,
                        "Esa tarea ya existe",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }
            }

            addTask(taskText)

            binding.inputTask.text.clear()
        }
    }

    // =======================================================
    // FUNCIONES DEL TEMPORIZADOR
    // =======================================================
    private fun startTimer() {
        countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerInterface()
            }

            override fun onFinish() {
                timerRunning = false
                timeLeftInMillis = START_TIME_IN_MILLIS
                sesionesCompletadas++
                updateTimerInterface()
                updateCountText()
                addHistoryItem() // Guarda en el historial
                Toast.makeText(
                    this@MainActivity,
                    "Pomodoro completado para:\n$activeTaskName",
                    Toast.LENGTH_LONG
                ).show()

                binding.btnStart.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnReset.isEnabled = false
            }
        }.start()
        timerRunning = true

        binding.btnStart.isEnabled = false
        binding.btnPause.isEnabled = true
        binding.btnResume.isEnabled = false
        binding.btnReset.isEnabled = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false

        binding.btnPause.isEnabled = false
        binding.btnResume.isEnabled = true
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        timeLeftInMillis = START_TIME_IN_MILLIS
        updateTimerInterface()

        binding.btnStart.isEnabled = true
        binding.btnPause.isEnabled = false
        binding.btnResume.isEnabled = false
        binding.btnReset.isEnabled = false
        binding.progressTimer.progress = 100
    }

    private fun updateTimerInterface() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeFormatted = String.format("%02d:%02d", minutes, seconds)

        binding.textTimer.text = timeFormatted

        // Actualiza la barrita roja de progreso
        val progress = (timeLeftInMillis.toFloat() / START_TIME_IN_MILLIS.toFloat() * 100).toInt()
        binding.progressTimer.progress = progress
    }

    // =======================================================
    // FUNCIONES DE TAREAS E HISTORIAL DINÁMICO
    // =======================================================
    private fun addTask(taskName: String) {

        // Oculta el mensaje de lista vacía
        binding.textEmptyTasks.visibility = View.GONE

        // Creamos la vista usando View Binding
        val taskBinding = ItemTaskBinding.inflate(layoutInflater)

        // Colocamos el nombre de la tarea
        taskBinding.tvTaskTitle.text = taskName

        // -------------------------------------------------
        // SELECCIONAR TAREA ACTIVA
        // -------------------------------------------------
        taskBinding.root.setOnClickListener {

            // Si ya había una tarea activa, le quitamos el color
            activeTask?.setBackgroundColor(android.graphics.Color.WHITE)

            // Guardamos la nueva tarea activa
            activeTask = taskBinding.tvTaskTitle
            activeTaskName = taskName

            // La pintamos para indicar que está seleccionada
            activeTask?.setBackgroundColor(android.graphics.Color.parseColor("#FFF59D"))

            Toast.makeText(this, "Tarea activa: $taskName", Toast.LENGTH_SHORT).show()
        }

        // -------------------------------------------------
        // CHECKBOX
        // -------------------------------------------------
        taskBinding.cbTask.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {

                taskBinding.tvTaskTitle.paintFlags =
                    taskBinding.tvTaskTitle.paintFlags or
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG

                taskBinding.tvTaskTitle.alpha = 0.5f

                tareasPendientes--

            } else {

                taskBinding.tvTaskTitle.paintFlags =
                    taskBinding.tvTaskTitle.paintFlags and
                            android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()

                taskBinding.tvTaskTitle.alpha = 1f

                tareasPendientes++

            }

            if (activeTask == taskBinding.tvTaskTitle) {
                activeTask = null
                activeTaskName = "Sin tarea seleccionada"
            }
            updateCountText()
        }

        // -------------------------------------------------
        // ELIMINAR
        // -------------------------------------------------
        taskBinding.btnDeleteTask.setOnClickListener {

            // Si no estaba completada, resta una pendiente
            if (!taskBinding.cbTask.isChecked) {
                tareasPendientes--
            }

            binding.containerTasks.removeView(taskBinding.root)

            totalTareas--
            // Si era la tarea activa
            if (activeTask == taskBinding.tvTaskTitle) {
                activeTask = null
                activeTaskName = "Sin tarea seleccionada"
            }

            updateCountText()

            // Mostrar mensaje si ya no hay tareas
            if (binding.containerTasks.childCount == 0) {
                binding.textEmptyTasks.visibility = View.VISIBLE
            }
        }

        // Agregamos la vista al contenedor
        binding.containerTasks.addView(taskBinding.root)

        tareasPendientes++
        totalTareas++
        updateCountText()
    }

    private fun addHistoryItem() {

        binding.textEmptyHistory.visibility = View.GONE

        val historyView = layoutInflater.inflate(R.layout.item_history, null)

        val tvHistoryItem =
            historyView.findViewById<TextView>(R.id.tv_history_item)

        tvHistoryItem.text =
            """
Sesión $sesionesCompletadas

Tarea:
$activeTaskName

Duración:
25 minutos
        """.trimIndent()

        binding.containerHistory.addView(historyView, 0)
    }

    private fun updateCountText() {

        binding.textSummary.text =
            """
Pendientes: $tareasPendientes

Total tareas: $totalTareas

Pomodoros completados: $sesionesCompletadas
        """.trimIndent()
    }
}