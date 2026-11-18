package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    indices = [
        Index(value = ["sourceDocumentId"]),
        Index(value = ["category"]),
        Index(value = ["isFavorite"])
    ]
)
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionText: String,
    val optionsText: String = "",
    val answerKey: String,
    val explanation: String = "",
    val category: String = "UMUM",
    val sourceDocumentId: Long = 1L,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
