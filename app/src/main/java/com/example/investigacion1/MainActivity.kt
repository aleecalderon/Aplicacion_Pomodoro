package com.example.investigacion1

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.investigacion1.databinding.ActivityMainBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            val taskText = binding.inputTask.text.toString()
            if (taskText.isNotEmpty()) {
                addTask(taskText) // Llama a la función para crear la tarea
                binding.inputTask.text.clear() // Limpia el cuadro de texto
            } else {
                // Muestra un mensajito si el usuario no escribió nada
                Toast.makeText(this, "Por favor escribe una tarea", Toast.LENGTH_SHORT).show()
            }
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
                Toast.makeText(this@MainActivity, "¡Pomodoro terminado!", Toast.LENGTH_LONG).show()
            }
        }.start()
        timerRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        timeLeftInMillis = START_TIME_IN_MILLIS
        updateTimerInterface()
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
        // Oculta el texto "No hay tareas creadas aún"
        binding.textEmptyTasks.visibility = View.GONE

        // Infla (crea) la vista usando tu archivo item_task.xml
        val taskView = layoutInflater.inflate(R.layout.item_task, null)

        // Busca los elementos dentro de esa nueva vista
        val tvTaskTitle = taskView.findViewById<TextView>(R.id.tv_task_title)
        val btnDeleteTask = taskView.findViewById<Button>(R.id.btn_delete_task)

        // Le pone el nombre que el usuario escribió
        tvTaskTitle.text = taskName

        // Lógica del botón "Eliminar" en cada tarea
        btnDeleteTask.setOnClickListener {
            binding.containerTasks.removeView(taskView) // Lo borra de la pantalla
            tareasPendientes--
            updateCountText()

            // Si ya no hay tareas, vuelve a mostrar el mensaje vacío
            if (tareasPendientes == 0) {
                binding.textEmptyTasks.visibility = View.VISIBLE
            }
        }

        // Agrega la tarea a la pantalla
        binding.containerTasks.addView(taskView)
        tareasPendientes++
        updateCountText()
    }

    private fun addHistoryItem() {
        binding.textEmptyHistory.visibility = View.GONE

        val historyView = layoutInflater.inflate(R.layout.item_history, null)
        val tvHistoryItem = historyView.findViewById<TextView>(R.id.tv_history_item)

        tvHistoryItem.text = "Sesión $sesionesCompletadas: Completada - 25 min"

        // Agrega el historial (el 0 hace que se ponga al principio de la lista)
        binding.containerHistory.addView(historyView, 0)
    }

    private fun updateCountText() {
        binding.textSummary.text = "Tareas pendientes: $tareasPendientes | Sesiones completadas: $sesionesCompletadas"
    }
}