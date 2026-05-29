package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // Lessons queries
    @Query("SELECT * FROM lessons ORDER BY id ASC")
    fun getAllLessons(): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id")
    suspend fun getLessonById(id: Int): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    // Quizzes queries
    @Query("SELECT * FROM quizzes WHERE lessonId = :lessonId")
    suspend fun getQuizzesForLesson(lessonId: Int): List<Quiz>

    @Query("SELECT * FROM quizzes ORDER BY id ASC")
    suspend fun getAllQuizzes(): List<Quiz>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizzes(quizzes: List<Quiz>)

    // User Profile queries
    @Query("SELECT * FROM user_profiles WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfile)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfile)

    // Feedbacks queries
    @Query("SELECT * FROM lesson_feedbacks ORDER BY timestamp DESC")
    fun getAllFeedbacks(): Flow<List<LessonFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: LessonFeedback)
    
    @Query("DELETE FROM lesson_feedbacks")
    suspend fun clearAllFeedbacks()
}
