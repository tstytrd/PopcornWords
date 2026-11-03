package com.example.popcornwords.data

object DolchWordsData {
    val prePrimer = listOf(
        "the", "to", "and", "a", "I", "you", "it", "in", "said", "for",
        "up", "look", "is", "go", "we", "little", "down", "can", "see", "not",
        "one", "my", "me", "big", "come", "blue", "red", "where", "jump", "away",
        "here", "help", "make", "yellow", "two", "play", "run", "find", "three", "funny"
    )

    val primer = listOf(
        "he", "was", "that", "she", "on", "they", "but", "at", "with", "all",
        "there", "out", "be", "have", "am", "do", "did", "what", "so", "get",
        "like", "this", "will", "yes", "went", "are", "now", "no", "came", "ride",
        "into", "good", "want", "too", "pretty", "four", "saw", "well", "ran", "brown",
        "eat", "who", "new", "must", "black", "white", "soon", "our", "ate", "say",
        "under", "please"
    )

    val firstGrade = listOf(
        "of", "his", "had", "him", "her", "some", "as", "then", "could", "when",
        "were", "them", "ask", "an", "over", "just", "from", "any", "how", "know",
        "put", "take", "every", "old", "by", "after", "think", "let", "going", "walk",
        "again", "may", "stop", "fly", "round", "give", "once", "open", "has", "live",
        "thank"
    )

    val secondGrade = listOf(
        "would", "very", "your", "its", "around", "don't", "right", "green", "their", "call",
        "sleep", "five", "wash", "or", "before", "been", "off", "cold", "tell", "work",
        "first", "does", "goes", "write", "always", "made", "gave", "us", "buy", "those",
        "use", "fast", "pull", "both", "sit", "which", "read", "why", "found", "because",
        "best", "upon", "these", "sing", "wish", "many"
    )

    val thirdGrade = listOf(
        "if", "long", "about", "got", "six", "never", "seven", "eight", "today", "myself",
        "much", "keep", "try", "start", "ten", "bring", "drink", "only", "better", "hold",
        "warm", "full", "done", "light", "pick", "hurt", "cut", "kind", "fall", "carry",
        "small", "own", "show", "hot", "far", "draw", "clean", "grow", "together", "shall",
        "laugh"
    )

    val allCategories = listOf("Pre-Primer", "Primer", "First Grade", "Second Grade", "Third Grade")

    fun getAllInitialWords(): List<WordEntity> {
        val list = mutableListOf<WordEntity>()
        prePrimer.forEach { list.add(WordEntity(word = it, listName = "Pre-Primer", isEnabled = true)) }
        primer.forEach { list.add(WordEntity(word = it, listName = "Primer", isEnabled = true)) }
        firstGrade.forEach { list.add(WordEntity(word = it, listName = "First Grade", isEnabled = true)) }
        secondGrade.forEach { list.add(WordEntity(word = it, listName = "Second Grade", isEnabled = true)) }
        thirdGrade.forEach { list.add(WordEntity(word = it, listName = "Third Grade", isEnabled = true)) }
        return list
    }
}
