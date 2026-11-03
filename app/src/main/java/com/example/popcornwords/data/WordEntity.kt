package com.example.popcornwords.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val word: String,
    val listName: String = "Pre-Primer", // "Pre-Primer", "Primer", "First Grade", "Second Grade", "Third Grade"
    val isEnabled: Boolean = true,       // Whether this word is quizzed in practice
    val isTricky: Boolean = false,       // Marked true if missed recently
    val timesPracticed: Int = 0,
    val timesCorrect: Int = 0
)
