package com.tusizi.sakuraword.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(wrongQuestion: WrongQuestionEntity)

    @Query("SELECT * FROM wrong_questions ORDER BY timestamp DESC")
    fun getAllWrongQuestions(): Flow<List<WrongQuestionEntity>>

    @Delete
    suspend fun delete(wrongQuestion: WrongQuestionEntity)
    
    @Query("DELETE FROM wrong_questions WHERE questionText = :text")
    suspend fun deleteByText(text: String)
}
