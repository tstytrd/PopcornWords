package com.example.popcornwords.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words WHERE listName = :listName")
    fun getWordsByList(listName: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE listName IN (:listNames) AND isEnabled = 1")
    fun getActiveWordsByCategories(listNames: List<String>): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isEnabled = 1")
    fun getAllActiveWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE isTricky = 1")
    fun getTrickyWords(): Flow<List<WordEntity>>

    @Query("SELECT DISTINCT listName FROM words")
    fun getAllListNames(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Insert
    suspend fun insertWord(word: WordEntity)

    @Insert
    suspend fun insertWords(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)

    @Query("UPDATE words SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateWordEnabled(id: Int, isEnabled: Boolean)

    @Query("UPDATE words SET isEnabled = :isEnabled WHERE listName = :category")
    suspend fun updateCategoryEnabled(category: String, isEnabled: Boolean)

    @Query("DELETE FROM words WHERE id = :id")
    suspend fun deleteWord(id: Int)

    @Query("DELETE FROM words WHERE id IN (:ids)")
    suspend fun deleteWords(ids: List<Int>)

    @Query("DELETE FROM words")
    suspend fun deleteAllWords()
}
