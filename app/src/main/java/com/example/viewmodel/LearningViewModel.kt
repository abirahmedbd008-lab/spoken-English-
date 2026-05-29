package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LearningRepository
import com.example.data.database.*
import com.example.network.GeminiRepository
import com.example.ui.EnglishSpeechManager
import com.example.ui.LingoNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface FeedbackState {
    object Idle : FeedbackState
    object Loading : FeedbackState
    data class Success(val text: String) : FeedbackState
    data class Error(val message: String) : FeedbackState
}

class LearningViewModel(
    application: Application,
    private val repository: LearningRepository
) : AndroidViewModel(application) {

    private val notificationHelper = LingoNotificationHelper(application)
    private val geminiRepository = GeminiRepository()
    private var speechManager: EnglishSpeechManager? = null

    // Real-time states
    val allLessons: StateFlow<List<Lesson>> = repository.allLessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val feedbacks: StateFlow<List<LessonFeedback>> = repository.allFeedbacks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active UX Navigation & Lesson States
    private val _selectedLesson = MutableStateFlow<Lesson?>(null)
    val selectedLesson: StateFlow<Lesson?> = _selectedLesson.asStateFlow()

    private val _activeQuizzes = MutableStateFlow<List<Quiz>>(emptyList())
    val activeQuizzes: StateFlow<List<Quiz>> = _activeQuizzes.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedAnswerOption = MutableStateFlow<String?>(null)
    val selectedAnswerOption: StateFlow<String?> = _selectedAnswerOption.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    private val _aiFeedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Idle)
    val aiFeedbackState: StateFlow<FeedbackState> = _aiFeedbackState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage: SharedFlow<String> = _toastMessage.asSharedFlow()

    // Wrong concept accumulator for Gemini reasoning
    private val wrongConcepts = mutableListOf<String>()

    fun initSpeechManager(speech: EnglishSpeechManager) {
        this.speechManager = speech
    }

    init {
        // Pre-populate Database and trigger mock check on startup
        viewModelScope.launch {
            repository.checkAndPrepopulateData()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectLesson(lesson: Lesson?) {
        _selectedLesson.value = lesson
        _quizCompleted.value = false
        _currentQuizIndex.value = 0
        _quizScore.value = 0
        _selectedAnswerOption.value = null
        _aiFeedbackState.value = FeedbackState.Idle
        wrongConcepts.clear()

        if (lesson != null) {
            viewModelScope.launch {
                val quizzes = repository.getQuizzesForLesson(lesson.id)
                _activeQuizzes.value = quizzes
            }
        } else {
            _activeQuizzes.value = emptyList()
        }
    }

    fun speak(text: String) {
        speechManager?.speak(text)
    }

    fun selectAnswer(option: String) {
        if (_selectedAnswerOption.value == null) {
            _selectedAnswerOption.value = option
        }
    }

    fun nextQuizOrFinish() {
        val quizzes = _activeQuizzes.value
        val currentIndex = _currentQuizIndex.value
        val selected = _selectedAnswerOption.value ?: return

        val currentQuiz = quizzes.getOrNull(currentIndex) ?: return
        val isCorrect = selected == currentQuiz.correctAnswer

        if (isCorrect) {
            _quizScore.value += 1
        } else {
            wrongConcepts.add(currentQuiz.question)
        }

        if (currentIndex + 1 < quizzes.size) {
            _currentQuizIndex.value += 1
            _selectedAnswerOption.value = null
        } else {
            // Finished Lesson Quiz block
            _quizCompleted.value = true
            completeLessonProgress()
        }
    }

    private fun completeLessonProgress() {
        val lesson = _selectedLesson.value ?: return
        val finalScore = _quizScore.value
        val totalQuizzes = _activeQuizzes.value.size

        viewModelScope.launch(Dispatchers.IO) {
            val profile = repository.getUserProfileDirect()
            
            // Mark lesson as completed if it isn't already
            val completedList = profile.completedLessons.split(",")
                .filter { it.isNotEmpty() }
                .toMutableSet()
            
            val isNewCompletion = completedList.add(lesson.id.toString())
            val xpGain = if (isNewCompletion) 50 else 10 // premium XP
            val scoreXp = finalScore * 10
            
            val updatedProfile = profile.copy(
                completedLessons = completedList.joinToString(","),
                totalXp = profile.totalXp + xpGain + scoreXp,
                streak = profile.streak + 1,
                isSynced = false
            )
            repository.updateProfile(updatedProfile)

            // Trigger Gemini Personalized AI Feedback
            _aiFeedbackState.value = FeedbackState.Loading
            val feedbackText = geminiRepository.getPersonalizedFeedback(
                preferredLanguage = updatedProfile.preferredLanguage,
                lessonTitle = lesson.title,
                score = finalScore,
                total = totalQuizzes,
                wrongConcepts = wrongConcepts
            )
            
            // Store feedback history offline
            val feedbackDb = LessonFeedback(
                lessonId = lesson.id,
                score = finalScore,
                totalQuestions = totalQuizzes,
                analysis = feedbackText
            )
            repository.saveFeedback(feedbackDb)
            _aiFeedbackState.value = FeedbackState.Success(feedbackText)
            
            _toastMessage.emit("লেসন সম্পূর্ণ! +${xpGain + scoreXp} XP অর্জিত হয়েছে।")
        }
    }

    fun triggerCloudSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            _toastMessage.emit("ক্লাউডের সাথে ডেটা সিঙ্ক্রোনাইজ হচ্ছে...")
            
            // Secure simulation of server sync - low-end delay optimized
            delay(2000)
            
            val profile = repository.getUserProfileDirect()
            val syncedProfile = profile.copy(
                isSynced = true,
                lastSyncTime = System.currentTimeMillis()
            )
            repository.updateProfile(syncedProfile)
            _isSyncing.value = false
            _toastMessage.emit("অটো-সিঙ্ক সফল হয়েছে! আপনার ডেটা ক্লাউডে সুরক্ষিত রাখা হয়েছে।")
        }
    }

    fun togglePreferredLanguage(language: String) {
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            repository.updateProfile(profile.copy(preferredLanguage = language))
            _toastMessage.emit("ভাষা পরিবর্তন করা হয়েছে: $language")
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            notificationHelper.sendReminderNotification(
                "📚 English Learner: Daily Reminder",
                "Hi, ready for a standard practice? Complete Lesson quiz today to keep your streak!"
            )
            _toastMessage.emit("ডেইলি রিমাইন্ডার নোটিফিকেশন পাঠানো হয়েছে!")
        }
    }

    fun updateProfileName(name: String, email: String) {
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            repository.updateProfile(profile.copy(name = name, email = email, isLoggedIn = true))
            _toastMessage.emit("আপনার প্রোফাইল সফলভাবে আপডেট হয়েছে এবং লগইন হয়েছে!")
        }
    }

    fun logout() {
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            repository.updateProfile(profile.copy(
                name = "Guest User",
                email = "guest@example.com",
                isLoggedIn = false
            ))
            _toastMessage.emit("লগআউট সম্পন্ন হয়েছে।")
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val profile = repository.getUserProfileDirect()
            repository.updateProfile(profile.copy(darkThemeEnabled = !profile.darkThemeEnabled))
        }
    }

    fun clearFeedbackHistory() {
        viewModelScope.launch {
            repository.clearFeedbacks()
            _toastMessage.emit("ফিডব্যাক ইতিহাস মুছে ফেলা হয়েছে!")
        }
    }
}
