package com.example.data

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class LearningRepository(private val learningDao: LearningDao) {

    val allLessons: Flow<List<Lesson>> = learningDao.getAllLessons()
        .onStart {
            // Check if db is empty and pre-populate if needed
        }

    val userProfileFlow: Flow<UserProfile?> = learningDao.getUserProfileFlow()
    
    val allFeedbacks: Flow<List<LessonFeedback>> = learningDao.getAllFeedbacks()

    suspend fun getLessonById(id: Int) = learningDao.getLessonById(id)

    suspend fun getQuizzesForLesson(lessonId: Int): List<Quiz> {
        val quizzes = learningDao.getQuizzesForLesson(lessonId)
        if (quizzes.isEmpty()) {
            // Return all quizzes as fallback
            return learningDao.getAllQuizzes().filter { it.lessonId == lessonId }
        }
        return quizzes
    }

    suspend fun updateProfile(profile: UserProfile) {
        learningDao.insertUserProfile(profile)
    }

    suspend fun getUserProfileDirect(): UserProfile {
        return learningDao.getUserProfile() ?: UserProfile().also {
            learningDao.insertUserProfile(it)
        }
    }

    suspend fun saveFeedback(feedback: LessonFeedback) {
        learningDao.insertFeedback(feedback)
    }

    suspend fun clearFeedbacks() {
        learningDao.clearAllFeedbacks()
    }

    // Static function to pre-populate database with content in Bengali & English
    suspend fun checkAndPrepopulateData() {
        val existingProfile = learningDao.getUserProfile()
        if (existingProfile == null) {
            learningDao.insertUserProfile(UserProfile(
                id = 1,
                name = "Abir Ahmed",
                email = "abir@example.com",
                isLoggedIn = false,
                completedLessons = "",
                level = "Beginner",
                streak = 3,
                totalXp = 120,
                preferredLanguage = "Bengali",
                notificationEnabled = true,
                notificationIntervalHours = 24,
                darkThemeEnabled = true,
                isSynced = true
            ))
        }

        // Check lessons, if empty populate
        learningDao.getLessonById(1) ?: run {
            val initialLessons = listOf(
                Lesson(
                    id = 1,
                    title = "Tenses (কাল বা সময়)",
                    category = "Grammar",
                    summary = "ক্রিয়া সম্পন্ন হওয়ার সময়কে Tense বলে। এটি ইংরেজি ব্যাকরণের অত্যন্ত গুরুত্বপূর্ণ অঙ্গ।",
                    content = """
                        Tense মূলত ৩ প্রকার:
                        ১. Present Tense (বর্তমান কাল)
                        ২. Past Tense (অতীত কাল)
                        ৩. Future Tense (ভবিষ্যত কাল)
                        
                        প্রতিটি Tense-কে আবার ৪টি ভাগে ভাগ করা যায়:
                        - Indefinite (সাধারণ)
                        - Continuous (ঘটমান)
                        - Perfect (পুরাঘটিত)
                        - Perfect Continuous (পুরাঘটিত ঘটমান)
                        
                        গুরুত্বপূর্ণ বর্তমানকালের গঠন (Structure):
                        * Subject + Verb (Present form) + Object.
                        যেমন: I eat rice.
                        * Continuous: Subject + am/is/are + Verb-ing.
                        যেমন: She is singing a song.
                    """.trimIndent(),
                    examples = "I eat rice|She is singing a song|He went to school yesterday|We will play tomorrow"
                ),
                Lesson(
                    id = 2,
                    title = "Noun & Pronoun (বিশেষ্য ও সর্বনাম)",
                    category = "Parts of Speech",
                    summary = "যে শব্দ দিয়ে কোন কিছুর নাম প্রকাশ পায় তাকে Noun বলে এবং Noun এর পরিবর্তে যা বসে তাই Pronoun।",
                    content = """
                        Noun (বিশেষ্য) ৫ প্রকার:
                        ১. Proper Noun (নির্দিষ্ট নাম) - যেমন: Dhaka, Abir.
                        ২. Common Noun (সাধারণ জাতিবাচক) - যেমন: Boy, City, Flower.
                        ৩. Collective Noun (সমষ্টিবাচক) - যেমন: Class, Team, Army.
                        ৪. Material Noun (পদার্থবাচক) - যেমন: Water, Gold, Iron.
                        ৫. Abstract Noun (গুণবাচক) - যেমন: Honesty, Kindness, Youth.

                        Pronoun (সর্বনাম) ৮ প্রকার:
                        - Personal: I, we, he, she, they
                        - Demonstrative: This, that, these, those
                        - Relative: Who, which, that, whom
                        - Interrogative: Who, what, which
                    """.trimIndent(),
                    examples = "Honesty is the best policy|Dhaka is a historical city|They are my classmates|She finished the work herself"
                ),
                Lesson(
                    id = 3,
                    title = "Prepositions (পদান্বয়ী অব্যয়)",
                    category = "Grammar Usage",
                    summary = "যেসব শব্দ Noun বা Pronoun এর পূর্বে বসে তার সাথে বাক্যের অন্যান্য শব্দের সম্পর্ক তৈরি করে তাদের Preposition বলে।",
                    content = """
                        প্রয়োজনীয় কিছু Prepositions এবং ব্যবহার:
                        * In: বড় স্থান, মাস, বা বছরের পূর্বে বসে। (e.g., In London, In December)
                        * At: ছোট স্থান, নির্দিষ্ট সময় ও নিখুঁত ঠিকানা বোঝাতে। (e.g., At 9 am, At home)
                        * On: কোন কিছুর উপরে স্পর্শ অবস্থায় বোঝাতে এবং দিন বা তারিখের পূর্বে বসে। (e.g., On Sunday, On the table)
                        * Into: বাহির থেকে ভেতরে প্রবেশ করতে বোঝায়। (e.g., He entered into the room)
                        
                        Appropriate Preposition উদাহরণ:
                        - Accustomed to (অভ্যস্ত)
                        - Aware of (সচেতন)
                        - Believe in (বিশ্বাস করা)
                    """.trimIndent(),
                    examples = "The book is on the table|He lives in Bangladesh|She goes to school at 9 AM|The cat jumped into the river"
                ),
                Lesson(
                    id = 4,
                    title = "Subject-Verb Agreement (কর্তা-ক্রিয়া সঙ্গতি)",
                    category = "Syntax Rules",
                    summary = "বাক্যের Subject এর Number এবং Person অনুযায়ী Verb এর সঠিক রূপ নির্ধারণের নিয়মাবলিকে Subject-Verb Agreement বলে।",
                    content = """
                        নিয়ম ১: Subject যদি Singular হয়, Verb-ও Singular হবে। Subject যদি Plural হয়, Verb-ও Plural হবে।
                        যেমন: The apple is delicious. apples are delicious.
                        
                        নিয়ম ২: দুটি Singular Subject যদি "and" দিয়ে যুক্ত হয়, তবে Verb সাধারণত Plural হবে।
                        যেমন: Jamal and Kamal are brothers. 
                        কিন্তু তারা যদি একই ধারণা প্রকাশ করে, তবে Verb Singular হবে। (যেমন: Bread and butter is my favorite foods)
                        
                        নিয়ম ৩: 'Each', 'Every', 'Either', 'Neither' বাক্যের শুরুতে থাকলে Singular Verb হবে।
                        যেমন: Each of the boys gets a book.
                    """.trimIndent(),
                    examples = "Either Jamal or I am responsible|Time and tide wait for none|Every student has to attend list|Bread and butter is my favorite break"
                )
            )
            learningDao.insertLessons(initialLessons)
        }

        // Populating Quizzes
        val allQuizzes = learningDao.getAllQuizzes()
        if (allQuizzes.isEmpty()) {
            val initialQuizzes = listOf(
                // Quizzes for Lesson 1 (Tense)
                Quiz(
                    id = 1,
                    lessonId = 1,
                    question = "He ________ (go) to school every day.",
                    optionA = "go",
                    optionB = "goes",
                    optionC = "going",
                    optionD = "went",
                    correctAnswer = "B",
                    explanation = "He হল Third Person Singular Subject। তাই Present Indefinite Tense অনুযায়ী Verb এর শেষে s/es যুক্ত হয়ে 'goes' হবে।"
                ),
                Quiz(
                    id = 2,
                    lessonId = 1,
                    question = "They __________ (play) football right now.",
                    optionA = "is playing",
                    optionB = "played",
                    optionC = "are playing",
                    optionD = "will play",
                    correctAnswer = "C",
                    explanation = "Right now থাকলে Present Continuous Tense হয়। They বহুবচন (Plural) হওয়ায় 'are playing' সঠিক উত্তর।"
                ),
                Quiz(
                    id = 3,
                    lessonId = 1,
                    question = "By the time I arrived, he __________ (leave) already.",
                    optionA = "has left",
                    optionB = "had left",
                    optionC = "leaves",
                    optionD = "was leaving",
                    correctAnswer = "B",
                    explanation = "অতীতের দুটি কাজের মধ্যে যেটি আগে হয়েছিল সেটি Past Perfect (had + verb-3) হয়। তাই 'had left' সঠিক।"
                ),
                // Quizzes for Lesson 2 (Noun/Pronoun)
                Quiz(
                    id = 4,
                    lessonId = 2,
                    question = "Which type of Noun is 'Honesty'?",
                    optionA = "Proper Noun",
                    optionB = "Collective Noun",
                    optionC = "Material Noun",
                    optionD = "Abstract Noun",
                    correctAnswer = "D",
                    explanation = "Honesty (সততা) একটি বিমূর্ত গুণ বা অনুভূতি যা স্পর্শ করা যায় না কিন্তু অনুভব করা যায়, তাই এটি Abstract Noun।"
                ),
                Quiz(
                    id = 5,
                    lessonId = 2,
                    question = "Select the relative pronoun from options:",
                    optionA = "She",
                    optionB = "Who",
                    optionC = "Them",
                    optionD = "Our",
                    correctAnswer = "B",
                    explanation = "'Who' হচ্ছে Relative Pronoun যা দুটি বাক্যাংশকে সংযুক্ত করার কাজ করতে পারে।"
                ),
                // Quizzes for Lesson 3 (Preposition)
                Quiz(
                    id = 6,
                    lessonId = 3,
                    question = "I am accustomed ________ doing hard exercises.",
                    optionA = "with",
                    optionB = "to",
                    optionC = "of",
                    optionD = "at",
                    correctAnswer = "B",
                    explanation = "'Accustomed to' এটি একটি appropriate preposition যার অর্থ অভ্যস্ত।"
                ),
                Quiz(
                    id = 7,
                    lessonId = 3,
                    question = "He came and sat ________ me.",
                    optionA = "besides",
                    optionB = "beside",
                    optionC = "by in",
                    optionD = "at",
                    correctAnswer = "B",
                    explanation = "'Beside' শব্দের অর্থ পাশে (by the side of)। অন্নদিকে 'Besides' অর্থ তদ্ব্যতীত বা অধিকন্তু। তাই 'beside' সঠিক।"
                ),
                // Quizzes for Lesson 4 (Subject Verb)
                Quiz(
                    id = 8,
                    lessonId = 4,
                    question = "The quality of these mangoes ________ very high.",
                    optionA = "is",
                    optionB = "are",
                    optionC = "were",
                    optionD = "have been",
                    correctAnswer = "A",
                    explanation = "এখানে প্রকৃত Subject হল 'The quality' (singular), 'mangoes' নয়। তাই Singular verb 'is' উপযুক্ত।"
                ),
                Quiz(
                    id = 9,
                    lessonId = 4,
                    question = "Bread and butter ________ my favorite breakfast.",
                    optionA = "are",
                    optionB = "is",
                    optionC = "were",
                    optionD = "have been",
                    correctAnswer = "B",
                    explanation = "Bread and butter দুটি ভিন্ন জিনিস হলেও একসাথে এক একক খাদ্য বোঝানোয় এটি singular ধারণা প্রকাশ করে ও Singular Verb ‘is’ গ্রহণ করে।"
                )
            )
            learningDao.insertQuizzes(initialQuizzes)
        }
    }
}
