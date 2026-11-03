<p align="center">
  <img src="popcorn_logo.jpg" alt="Popcorn Words Logo" width="200" style="border-radius: 32px;" />
</p>

<h1 align="center">🍿 Popcorn Words</h1>

<p align="center">
  <strong>A fun, kid-friendly Android app for practicing Dolch sight words through speech recognition.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green?logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin" alt="Language" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose" alt="UI Framework" />
  <img src="https://img.shields.io/badge/Database-Room-orange" alt="Database" />
  <img src="https://img.shields.io/badge/Min%20SDK-26-yellow" alt="Min SDK" />
</p>

---

## 📖 About

**Popcorn Words** is a native Android application designed to help early readers (Pre-K through 3rd grade) master the **220 Dolch sight words** — the most common words in English that children are expected to recognize on sight.

Kids practice by holding a walkie-talkie-style microphone button and saying each word aloud. The app uses Android's built-in speech recognition to verify pronunciation and gives instant, encouraging feedback. Words that are missed get automatically reshuffled to the back of the deck, creating a **mastery loop** that ensures every word is spoken correctly before the session ends.

Parents have full control through a **math-gated dashboard** where they can select which grade levels and individual words to quiz, track tricky words, and import custom word lists.

---

## ✨ Features

### 🎤 Speech-Powered Practice
- **Hold-to-talk microphone button** — press and hold to speak, release to submit
- **Real-time speech recognition** using Android's built-in `SpeechRecognizer`
- **Animated star celebration** (⭐) on correct answers
- **Encouraging feedback** on incorrect attempts — never punishing, always supportive
- **"Pass for now 🍿" button** to skip a tough word and come back to it later

### 📚 220 Dolch Words by Grade Level
All words are organized by the **Dolch Word List in Order of Frequency**:

| Grade Level | Word Count | Examples |
|---|---|---|
| **Pre-Primer** | 40 | the, to, and, a, I, you, it, in, said, for... |
| **Primer** | 52 | he, was, that, she, on, they, but, at, with... |
| **First Grade** | 41 | of, his, had, him, her, some, as, then, could... |
| **Second Grade** | 46 | would, very, your, its, around, don't, right... |
| **Third Grade** | 41 | if, long, about, got, six, never, seven, eight... |

### 🔄 Mastery Loop
- Wrong words are reshuffled to the back of the deck
- Sessions only end when **every active word** has been spoken correctly
- Words missed on the first try are automatically flagged as **"Tricky Words"** (⚠️)
- Practice stats (correct/practiced) are tracked per word

### 👨‍👩‍👧 Parent Dashboard
- **Math problem gate** (e.g., "What is 8 + 4?") prevents accidental child access
- **Grade-level tabs** with scrollable category selection
- **Per-word toggle switches** to include/exclude individual words from practice
- **"Turn All On / Turn All Off"** button per grade level
- **Tricky Words section** with clear badges highlighting problem words
- **Add custom words** manually to any grade
- **CSV/TXT file import** for bulk word additions
- **Reset button** to restore all 220 original Dolch words
- **Multi-select deletion** with floating action button

### 🌗 Dark Mode Support
- Full Material 3 dynamic theming
- High-contrast colors optimized for both light and dark modes
- Responsive font sizing that adapts to word length

---

## 🏗️ Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Database** | Room (SQLite) |
| **Speech Recognition** | Android `SpeechRecognizer` API |
| **Navigation** | Jetpack Navigation Compose |
| **Architecture** | Single-Activity with Composable screens |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 34 (Android 14) |

---

## 📁 Project Structure

```
PopcornWords/
├── app/src/main/
│   ├── java/com/example/popcornwords/
│   │   ├── MainActivity.kt              # All UI screens (Main Menu, Practice, Parent Dashboard)
│   │   ├── PopcornWordsApplication.kt    # App startup & database seeding
│   │   └── data/
│   │       ├── AppDatabase.kt            # Room database configuration
│   │       ├── WordDao.kt                # Data access queries
│   │       ├── WordEntity.kt             # Word schema (word, grade, enabled, tricky, stats)
│   │       └── DolchWordsData.kt         # Complete 220 Dolch words by grade
│   ├── res/
│   │   ├── drawable/
│   │   │   └── popcorn_logo.jpg          # App logo (in-app display)
│   │   ├── mipmap-*/                     # Launcher icons (all densities)
│   │   └── values/
│   │       └── colors.xml                # Theme colors
│   └── AndroidManifest.xml               # Permissions & app config
└── popcorn_logo.jpg                      # Logo for README
```

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio) (Hedgehog or later)
- Android device or emulator running **Android 8.0+ (API 26+)**
- Microphone access (for speech recognition)

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/PopcornWords.git
   ```
2. Open the project in **Android Studio**
3. Connect an Android device or start an emulator
4. Click **Run ▶️**

> **Note:** Speech recognition requires Google Play Services on most devices. It works best on a physical device with an internet connection for the first use.

---

## 🔒 Permissions

| Permission | Purpose |
|---|---|
| `RECORD_AUDIO` | Required for the speech recognition microphone |

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 🙏 Acknowledgments

- **Dolch Word List** — Edward William Dolch, Ph.D. (1936)
- **Word frequency ordering** — *The Jumbo Book of Sight Word Practice Pages* by Immacula A. Rhodes, Scholastic Teaching Resources (2013)
- Built with ❤️ for early readers everywhere
