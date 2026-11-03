package com.example.popcornwords

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.popcornwords.data.AppDatabase
import com.example.popcornwords.data.DolchWordsData
import com.example.popcornwords.data.WordDao
import com.example.popcornwords.data.WordEntity
import com.example.popcornwords.ui.theme.PopcornWordsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var wordDao: WordDao

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        wordDao = AppDatabase.getDatabase(this).wordDao()

        setContent {
            PopcornWordsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation(speechRecognizer, wordDao)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

// ─────────────────────────────────────────────
// NAVIGATION
// ─────────────────────────────────────────────
@Composable
fun AppNavigation(speechRecognizer: SpeechRecognizer, wordDao: WordDao) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu")        { MainMenuScreen(navController, wordDao) }
        composable("practice")         { PracticeScreen(speechRecognizer, navController, wordDao) }
        composable("parent_dashboard") { ParentDashboardScreen(navController, wordDao) }
    }
}

// ─────────────────────────────────────────────
// MAIN MENU
// ─────────────────────────────────────────────
@Composable
fun MainMenuScreen(navController: NavController, wordDao: WordDao) {
    var showMathGate by remember { mutableStateOf(false) }
    val activeWords by wordDao.getAllActiveWords().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.popcorn_logo),
            contentDescription = "Popcorn Words Logo",
            modifier = Modifier
                .size(160.dp)
                .clip(RoundedCornerShape(32.dp))
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text("🍿 Popcorn Words", fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2196F3), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${activeWords.size} words active for practice",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = { navController.navigate("practice") },
            modifier = Modifier.fillMaxWidth(0.85f).height(88.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            enabled = activeWords.isNotEmpty()
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(44.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Practice!", fontSize = 30.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = { showMathGate = true },
            modifier = Modifier.fillMaxWidth(0.85f).height(74.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Parent Mode", fontSize = 22.sp)
        }
    }

    if (showMathGate) {
        MathGateDialog(onDismiss = { showMathGate = false }, onSuccess = {
            showMathGate = false
            navController.navigate("parent_dashboard")
        })
    }
}

// ─────────────────────────────────────────────
// MATH GATE DIALOG
// ─────────────────────────────────────────────
@Composable
fun MathGateDialog(onDismiss: () -> Unit, onSuccess: () -> Unit) {
    val num1 by remember { mutableIntStateOf((3..12).random()) }
    val num2 by remember { mutableIntStateOf((3..12).random()) }
    val expectedAnswer = num1 + num2
    var userAnswer by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Parent Gate 🔒") },
        text = {
            Column {
                Text("Solve this problem to continue:", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("What is $num1 + $num2?", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = userAnswer,
                    onValueChange = { userAnswer = it; showError = false },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Your answer") },
                    isError = showError,
                    singleLine = true
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Incorrect answer, try again!", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (userAnswer.trim().toIntOrNull() == expectedAnswer) onSuccess()
                else { showError = true; userAnswer = "" }
            }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─────────────────────────────────────────────
// PRACTICE SCREEN (Mastery Loop + Tricky Words)
// ─────────────────────────────────────────────
@Composable
fun PracticeScreen(speechRecognizer: SpeechRecognizer, navController: NavController, wordDao: WordDao) {
    val activeEntities by wordDao.getAllActiveWords().collectAsState(initial = emptyList())

    val masteryQueue = remember { mutableStateListOf<WordEntity>() }
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize the queue once when words are first loaded from the database
    LaunchedEffect(activeEntities) {
        if (!isInitialized && activeEntities.isNotEmpty()) {
            masteryQueue.addAll(activeEntities.shuffled())
            isInitialized = true
        }
    }

    var sessionComplete by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var isTransitioning by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }
    var feedbackColor by remember { mutableStateOf(Color.Gray) }
    var showStar by remember { mutableStateOf(false) }
    var firstAttemptThisWord by remember { mutableStateOf(true) }

    // Active word on top of deck
    val currentEntity = masteryQueue.firstOrNull()
    val currentWord = currentEntity?.word ?: ""

    // Explicitly freeze target when user presses mic button
    var wordBeingTested by remember { mutableStateOf("") }
    val latestWordBeingTested by rememberUpdatedState(wordBeingTested)
    val latestCurrentEntity by rememberUpdatedState(currentEntity)
    val latestFirstAttempt by rememberUpdatedState(firstAttemptThisWord)
    val latestShowStar by rememberUpdatedState(showStar)
    val latestIsTransitioning by rememberUpdatedState(isTransitioning)

    val coroutineScope = rememberCoroutineScope()
    var transitionJob by remember { mutableStateOf<Job?>(null) }

    val speechIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }
    }

    DisposableEffect(Unit) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { isListening = false }

            override fun onError(error: Int) {
                isListening = false
                if (latestShowStar || latestIsTransitioning) return

                val errorMsg = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that. Try again! 🎙️"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "I didn't hear anything. 🎙️"
                    SpeechRecognizer.ERROR_CLIENT -> return
                    else -> "Couldn't hear clearly. Try again! 🎙️"
                }
                feedbackMessage = errorMsg
                feedbackColor = Color(0xFFFFA500)
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                if (latestIsTransitioning) return

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val target = latestWordBeingTested.lowercase().trim()
                    if (target.isEmpty()) return

                    val isCorrect = matches.any { match ->
                        val spokenWords = match.lowercase()
                            .replace(Regex("[^a-z0-9 ]"), "")
                            .split("\\s+".toRegex())
                        spokenWords.contains(target) || match.lowercase().trim() == target
                    }

                    if (isCorrect) {
                        isTransitioning = true
                        val entityToUpdate = latestCurrentEntity
                        if (entityToUpdate != null) {
                            if (!latestFirstAttempt) {
                                coroutineScope.launch(Dispatchers.IO) {
                                    wordDao.updateWord(entityToUpdate.copy(isTricky = true, timesPracticed = entityToUpdate.timesPracticed + 1))
                                }
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    wordDao.updateWord(entityToUpdate.copy(timesCorrect = entityToUpdate.timesCorrect + 1, timesPracticed = entityToUpdate.timesPracticed + 1))
                                }
                            }
                        }

                        feedbackMessage = "Great job! ⭐"
                        feedbackColor = Color(0xFF4CAF50)
                        showStar = true

                        transitionJob?.cancel()
                        transitionJob = coroutineScope.launch {
                            delay(1100)
                            showStar = false
                            if (masteryQueue.isNotEmpty()) {
                                masteryQueue.removeAt(0)
                            }
                            firstAttemptThisWord = true
                            feedbackMessage = ""
                            wordBeingTested = ""
                            isTransitioning = false
                            if (masteryQueue.isEmpty()) sessionComplete = true
                        }
                    } else {
                        firstAttemptThisWord = false
                        val entityToUpdate = latestCurrentEntity
                        if (entityToUpdate != null) {
                            coroutineScope.launch(Dispatchers.IO) {
                                wordDao.updateWord(entityToUpdate.copy(isTricky = true, timesPracticed = entityToUpdate.timesPracticed + 1))
                            }
                        }
                        val bestGuess = matches[0].lowercase().trim()
                        feedbackMessage = "Almost! You said \"$bestGuess\". Try again! 💪"
                        feedbackColor = Color(0xFF2196F3)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(listener)
        onDispose {
            transitionJob?.cancel()
            speechRecognizer.cancel()
        }
    }

    if (sessionComplete) {
        SessionCompleteScreen(
            onPlayAgain = {
                masteryQueue.addAll(activeEntities.shuffled())
                sessionComplete = false
                firstAttemptThisWord = true
                isTransitioning = false
            },
            onGoHome = { navController.navigate("main_menu") { popUpTo("main_menu") { inclusive = true } } }
        )
        return
    }

    if (masteryQueue.isEmpty() && !sessionComplete) {
        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No words selected for practice!", fontSize = 24.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Go to Parent Mode to choose words or grades to practice.", fontSize = 16.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.navigate("parent_dashboard") }) {
                    Text("Go to Parent Mode")
                }
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${activeEntities.size - masteryQueue.size} / ${activeEntities.size}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (currentEntity != null) {
                        Text(
                            text = currentEntity.listName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(48.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedVisibility(visible = feedbackMessage.isNotEmpty()) {
                    Text(
                        text = feedbackMessage,
                        color = feedbackColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val wordFontSize = when {
                    currentWord.length <= 3 -> 96.sp
                    currentWord.length <= 5 -> 76.sp
                    currentWord.length <= 7 -> 62.sp
                    else -> 48.sp
                }

                Text(
                    text = currentWord,
                    fontSize = wordFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )

                if (masteryQueue.size > 1 && !isTransitioning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = {
                            if (masteryQueue.isNotEmpty()) {
                                val wordToRetry = masteryQueue.removeAt(0)
                                masteryQueue.add(wordToRetry)
                                firstAttemptThisWord = true
                                feedbackMessage = ""
                                wordBeingTested = ""
                            }
                        }
                    ) {
                        Text("Pass for now 🍿", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 48.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (isTransitioning) "Next word coming..." else "Hold to talk",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                val buttonColor by animateColorAsState(
                    targetValue = when {
                        isTransitioning -> Color.Gray
                        isListening -> Color(0xFFD32F2F)
                        else -> Color(0xFFE91E63)
                    },
                    label = "buttonColor"
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(140.dp)
                        .background(buttonColor, CircleShape)
                        .pointerInput(isTransitioning) {
                            if (isTransitioning) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    if (!isTransitioning && currentWord.isNotEmpty()) {
                                        wordBeingTested = currentWord
                                        isListening = true
                                        feedbackMessage = "Listening... 🎙️"
                                        feedbackColor = Color.Gray
                                        speechRecognizer.startListening(speechIntent)
                                        tryAwaitRelease()
                                        if (isListening) {
                                            speechRecognizer.stopListening()
                                        }
                                        isListening = false
                                    }
                                }
                            )
                        }
                ) {
                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Microphone", tint = Color.White, modifier = Modifier.size(80.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = showStar,
            enter = scaleIn(initialScale = 0.3f) + fadeIn(),
            exit = scaleOut(targetScale = 2f) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("⭐", fontSize = 160.sp)
        }
    }
}

// ─────────────────────────────────────────────
// SESSION COMPLETE SCREEN
// ─────────────────────────────────────────────
@Composable
fun SessionCompleteScreen(onPlayAgain: () -> Unit, onGoHome: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 100.sp, modifier = Modifier.scale(scale))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Amazing job!", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text("You read every word!", fontSize = 26.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(64.dp))

        Button(
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth(0.8f).height(80.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Play Again! 🍿", fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth(0.8f).height(70.dp)
        ) {
            Text("Go Home", fontSize = 22.sp)
        }
    }
}

// ─────────────────────────────────────────────
// PARENT DASHBOARD (Dolch Grade Categories + Toggle Quizzing)
// ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(navController: NavController, wordDao: WordDao) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var selectedGrade by remember { mutableStateOf("Pre-Primer") }
    val wordsInGrade by wordDao.getWordsByList(selectedGrade).collectAsState(initial = emptyList())
    val trickyWords by wordDao.getTrickyWords().collectAsState(initial = emptyList())
    val allWords by wordDao.getAllWords().collectAsState(initial = emptyList())

    var newWordText by remember { mutableStateOf("") }
    var importMessage by remember { mutableStateOf("") }

    // ── Multi-select delete state (per grade) ──
    var selectedForDeleteIds by remember { mutableStateOf(setOf<Int>()) }
    val isDeleteSelectionMode = selectedForDeleteIds.isNotEmpty()

    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val wordsToInsert = mutableListOf<WordEntity>()
                    inputStream?.bufferedReader()?.useLines { lines ->
                        lines.forEach { line ->
                            line.split(",").forEach { part ->
                                val cleanWord = part.trim()
                                if (cleanWord.isNotEmpty()) {
                                    wordsToInsert.add(WordEntity(word = cleanWord, listName = selectedGrade, isEnabled = true))
                                }
                            }
                        }
                    }
                    if (wordsToInsert.isNotEmpty()) {
                        wordDao.insertWords(wordsToInsert)
                        importMessage = "✅ Imported ${wordsToInsert.size} words into $selectedGrade!"
                    } else {
                        importMessage = "⚠️ No words found in file."
                    }
                } catch (e: Exception) {
                    importMessage = "❌ Error reading file."
                }
            }
        }
    }

    val enabledInGradeCount = wordsInGrade.count { it.isEnabled }
    val allInGradeEnabled = wordsInGrade.isNotEmpty() && enabledInGradeCount == wordsInGrade.size

    val isDark = isSystemInDarkTheme()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isDeleteSelectionMode) Text("${selectedForDeleteIds.size} to delete", fontWeight = FontWeight.Bold)
                    else Text("Parent Dashboard")
                },
                navigationIcon = {
                    if (isDeleteSelectionMode) {
                        IconButton(onClick = { selectedForDeleteIds = emptySet() }) {
                            Icon(Icons.Default.Close, "Cancel deletion")
                        }
                    } else {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (!isDeleteSelectionMode) {
                        // Reset all 220 Dolch words button
                        IconButton(onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                wordDao.deleteAllWords()
                                wordDao.insertWords(DolchWordsData.getAllInitialWords())
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset all 220 Dolch Words", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isDeleteSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            wordDao.deleteWords(selectedForDeleteIds.toList())
                            selectedForDeleteIds = emptySet()
                        }
                    },
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Delete, "Delete selected") },
                    text = { Text("Delete ${selectedForDeleteIds.size} words") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Grade Category Selector (Scrollable Tab Row) ──
            item {
                Text(
                    text = "Select Grade Level to Customize",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                ScrollableTabRow(
                    selectedTabIndex = DolchWordsData.allCategories.indexOf(selectedGrade).coerceAtLeast(0),
                    edgePadding = 0.dp
                ) {
                    DolchWordsData.allCategories.forEach { grade ->
                        val activeCount = allWords.count { it.listName == grade && it.isEnabled }
                        val totalCount = allWords.count { it.listName == grade }
                        Tab(
                            selected = selectedGrade == grade,
                            onClick = {
                                selectedGrade = grade
                                selectedForDeleteIds = emptySet()
                            },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(grade, fontWeight = if (selectedGrade == grade) FontWeight.Bold else FontWeight.Normal)
                                    Text(
                                        "$activeCount/$totalCount on",
                                        fontSize = 11.sp,
                                        color = if (activeCount > 0) Color(0xFF4CAF50) else Color.Gray
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // ── Grade Level Summary & Quick Toggle All ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$selectedGrade List",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$enabledInGradeCount of ${wordsInGrade.size} words active for practice",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                val nextState = !allInGradeEnabled
                                coroutineScope.launch(Dispatchers.IO) {
                                    wordDao.updateCategoryEnabled(selectedGrade, nextState)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (allInGradeEnabled) Color(0xFFD32F2F) else Color(0xFF4CAF50)
                            )
                        ) {
                            Text(if (allInGradeEnabled) "Turn All Off" else "Turn All On", fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Add Custom Word to Current Grade ──
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Add Word to $selectedGrade", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newWordText,
                                onValueChange = { newWordText = it },
                                label = { Text("Word") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                if (newWordText.isNotBlank()) {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        wordDao.insertWord(WordEntity(word = newWordText.trim(), listName = selectedGrade, isEnabled = true))
                                    }
                                    newWordText = ""
                                }
                            }) { Text("Add") }
                        }
                    }
                }
            }

            // ── Tricky Words in This Category ──
            val trickyInGrade = trickyWords.filter { it.listName == selectedGrade }
            if (trickyInGrade.isNotEmpty()) {
                item {
                    Text(
                        "⚠️ Tricky Words in $selectedGrade (${trickyInGrade.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
                    )
                }
                items(trickyInGrade, key = { it.id }) { entity ->
                    DolchWordRow(
                        entity = entity,
                        isSelectedForDelete = entity.id in selectedForDeleteIds,
                        onToggleQuizzed = {
                            coroutineScope.launch(Dispatchers.IO) {
                                wordDao.updateWordEnabled(entity.id, !entity.isEnabled)
                            }
                        },
                        onToggleDeleteSelect = {
                            selectedForDeleteIds = if (entity.id in selectedForDeleteIds) selectedForDeleteIds - entity.id else selectedForDeleteIds + entity.id
                        },
                        onDelete = { coroutineScope.launch(Dispatchers.IO) { wordDao.deleteWord(entity.id) } },
                        onClearTricky = { coroutineScope.launch(Dispatchers.IO) { wordDao.updateWord(entity.copy(isTricky = false)) } }
                    )
                }
            }

            // ── Words in Current Grade (Order of Frequency) ──
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "$selectedGrade Words (Frequency Order)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        "Toggle switch to quiz",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(wordsInGrade, key = { it.id }) { entity ->
                DolchWordRow(
                    entity = entity,
                    isSelectedForDelete = entity.id in selectedForDeleteIds,
                    onToggleQuizzed = {
                        coroutineScope.launch(Dispatchers.IO) {
                            wordDao.updateWordEnabled(entity.id, !entity.isEnabled)
                        }
                    },
                    onToggleDeleteSelect = {
                        selectedForDeleteIds = if (entity.id in selectedForDeleteIds) selectedForDeleteIds - entity.id else selectedForDeleteIds + entity.id
                    },
                    onDelete = { coroutineScope.launch(Dispatchers.IO) { wordDao.deleteWord(entity.id) } }
                )
            }

            // ── Import from File section at bottom ──
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Import CSV / TXT to $selectedGrade", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("One word per line, or comma-separated.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(onClick = { csvLauncher.launch("*/*") }) {
                            Icon(Icons.Default.UploadFile, contentDescription = "Import")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import into $selectedGrade")
                        }
                        if (importMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(importMessage, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// ─────────────────────────────────────────────
// DOLCH WORD ROW (Quiz Switch + Tricky Badge + Accuracy)
// ─────────────────────────────────────────────
@Composable
fun DolchWordRow(
    entity: WordEntity,
    isSelectedForDelete: Boolean,
    onToggleQuizzed: () -> Unit,
    onToggleDeleteSelect: () -> Unit,
    onDelete: () -> Unit,
    onClearTricky: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = when {
        isSelectedForDelete -> MaterialTheme.colorScheme.errorContainer
        entity.isTricky     -> if (isDark) Color(0xFF4E342E) else Color(0xFFFFF3E0)
        !entity.isEnabled   -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        else                -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val textColor = when {
        isSelectedForDelete -> MaterialTheme.colorScheme.onErrorContainer
        entity.isTricky     -> if (isDark) Color(0xFFFFB74D) else Color(0xFFE65100)
        !entity.isEnabled   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        else                -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Switch to enable / disable quizzing this specific word
        Switch(
            checked = entity.isEnabled,
            onCheckedChange = { onToggleQuizzed() },
            modifier = Modifier.scale(0.8f)
        )
        Spacer(modifier = Modifier.width(4.dp))

        if (entity.isTricky) {
            Text("⚠️ ", fontSize = 16.sp)
        }

        Text(
            text = entity.word,
            fontSize = 20.sp,
            fontWeight = if (entity.isEnabled) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (entity.timesPracticed > 0) {
            Text(
                text = "${entity.timesCorrect}/${entity.timesPracticed}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (onClearTricky != null) {
            IconButton(onClick = onClearTricky, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Clear tricky", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
        }
    }
}
