package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDocumentDao {
    @Query("SELECT * FROM source_documents ORDER BY importedAt DESC, id DESC")
    fun getAllDocuments(): Flow<List<SourceDocumentEntity>>

    @Query("SELECT COUNT(*) FROM source_documents")
    fun getDocumentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM source_documents")
    suspend fun countSync(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: SourceDocumentEntity): Long

    @Query("UPDATE source_documents SET questionCount = questionCount + :delta WHERE id = :id")
    suspend fun incrementQuestionCount(id: Long, delta: Int)

    @Query("DELETE FROM source_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)
}
