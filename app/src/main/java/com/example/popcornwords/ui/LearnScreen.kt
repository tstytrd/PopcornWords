package com.example.popcornwords.ui

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.popcornwords.data.WordDao
import kotlinx.coroutines.delay

@Composable
fun LearnScreen(navController: NavController, wordDao: WordDao, tts: TextToSpeech?) {
    val activeEntities by wordDao.getAllActiveWords().collectAsState(initial = emptyList())
    val grades = activeEntities.map { it.listName }.distinct()
    var selectedGrade by remember { mutableStateOf("") }

    LaunchedEffect(grades) {
        if (selectedGrade.isEmpty() && grades.isNotEmpty()) {
            selectedGrade = grades.first()
        }
    }

    val wordsForGrade = activeEntities.filter { it.listName == selectedGrade }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Learn Words",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        if (grades.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = grades.indexOf(selectedGrade).coerceAtLeast(0),
                edgePadding = 0.dp,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                grades.forEach { grade ->
                    val count = activeEntities.count { it.listName == grade }
                    Tab(
                        selected = selectedGrade == grade,
                        onClick = { selectedGrade = grade },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = grade,
                                    fontWeight = if (selectedGrade == grade) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "$count words",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap any word to hear it!",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (wordsForGrade.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No words active in this grade.",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(wordsForGrade, key = { it.id }) { entity ->
                    LearnWordCard(word = entity.word, tts = tts)
                }
            }
        }
    }
}

@Composable
fun LearnWordCard(word: String, tts: TextToSpeech?) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    val wordFontSize = when {
        word.length <= 3 -> 28.sp
        word.length <= 5 -> 24.sp
        word.length <= 7 -> 20.sp
        else -> 16.sp
    }

    Card(
        modifier = Modifier
            .scale(scale)
            .fillMaxWidth()
            .height(80.dp)
            .clickable {
                isPressed = true
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, word)
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = word,
                    fontSize = wordFontSize,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Hear word",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(200)
            isPressed = false
        }
    }
}
