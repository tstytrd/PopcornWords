package com.example.popcornwords

import android.app.Application
import com.example.popcornwords.data.AppDatabase
import com.example.popcornwords.data.DolchWordsData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PopcornWordsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Seed the database with all 220 Dolch sight words grouped by grade
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(this@PopcornWordsApplication)
            val dao = db.wordDao()
            val existingPrePrimer = dao.getWordsByList("Pre-Primer").first()
            if (existingPrePrimer.isEmpty()) {
                // Remove legacy unclassified popcorn words if any
                dao.deleteAllWords()
                dao.insertWords(DolchWordsData.getAllInitialWords())
            }
        }
    }
}
