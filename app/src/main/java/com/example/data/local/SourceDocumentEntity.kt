package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "source_documents")
data class SourceDocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val format: String,
    val questionCount: Int,
    val importedAt: Long = System.currentTimeMillis()
)
