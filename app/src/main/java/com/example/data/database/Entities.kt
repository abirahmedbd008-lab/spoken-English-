package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val summary: String,
    val content: String,
    val examples: String // Pipe-separated examples: "I go to school | She sings a song"
)

@Entity(tableName = "quizzes")
data class Quiz(
    @PrimaryKey val id: Int,
    val lessonId: Int,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C" or "D"
    val explanation: String
)

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Guest User",
    val email: String = "guest@example.com",
    val isLoggedIn: Boolean = false,
    val completedLessons: String = "", // Comma-separated list like "1,2"
    val level: String = "Beginner",
    val streak: Int = 1,
    val totalXp: Int = 0,
    val preferredLanguage: String = "Bengali", // "Bengali", "Spanish", "Hindi"
    val notificationEnabled: Boolean = true,
    val notificationIntervalHours: Int = 24,
    val darkThemeEnabled: Boolean = true,
    val lastSyncTime: Long = 0L,
    val isSynced: Boolean = true
)

@Entity(tableName = "lesson_feedbacks")
data class LessonFeedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lessonId: Int,
    val score: Int,
    val totalQuestions: Int,
    val analysis: String,
    val timestamp: Long = System.currentTimeMillis()
)
