package com.example.tasktimer

import java.io.Serializable

data class Task(
    val id: Long,
    val name: String,
    val elapsedMillis: Long = 0L,
    val isCompleted: Boolean = false
) : Serializable
