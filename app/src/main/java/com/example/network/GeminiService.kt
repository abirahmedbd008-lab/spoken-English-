package com.example.network

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiRepository {
    suspend fun getPersonalizedFeedback(
        preferredLanguage: String,
        lessonTitle: String,
        score: Int,
        total: Int,
        wrongConcepts: List<String>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_API_KEY_HERE") {
            // Provide localized premium mock response if API Key is not configured
            return getLocalFeedbackFallback(preferredLanguage, lessonTitle, score, total)
        }

        val prompt = """
            You are an expert friendly English Language Mentor. Provide short, visually engaging, personalized feedback in Bengali and English mixed language (with English examples) based on a quiz performance:
            - Lesson: $lessonTitle
            - Score: $score out of $total
            - Language output preference: $preferredLanguage (Provide most of your explanations in Bengali with English phonetic grammar tips and practical examples).
            - Topic challenges: ${if (wrongConcepts.isNotEmpty()) wrongConcepts.joinToString() else "None! Perfect understanding."}
            
            Format instructions:
            - Keep it encouraging, upbeat, and structured in absolute maximum 3-4 bullet points.
            - Provide 1 actionable correction tip with a real-world example English sentence.
            - Strictly keep it under 150 words.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: getLocalFeedbackFallback(preferredLanguage, lessonTitle, score, total)
        } catch (e: Exception) {
            e.printStackTrace()
            getLocalFeedbackFallback(preferredLanguage, lessonTitle, score, total)
        }
    }

    private fun getLocalFeedbackFallback(lang: String, lesson: String, score: Int, total: Int): String {
        val ratio = score.toFloat() / total
        return when {
            ratio == 1.0f -> {
                """
                🎉 অসাধারণ! আপনি 100% স্কোর করেছেন।
                📝 $lesson টপিকটি আপনার সম্পূর্ণ আয়ত্তে এসেছে। 
                💡 পরবর্তী ধাপ: নতুন কুইজগুলোতে অংশ নিয়ে আপনার দক্ষতা আরও মজবুত রাখুন। বন্ধুদের সাথে শেয়ার করতে ভুলবেন না!
                """.trimIndent()
            }
            ratio >= 0.7f -> {
                """
                👏 চমৎকার প্রচেষ্টা! আপনি প্রায় সবগুলো উত্তরই সঠিক দিয়েছেন।
                👨‍🏫 $lesson বিষয়ের কিছু ছোট ত্রুটি আপনি অনুশীলনের মাধ্যমে দ্রুত ঠিক করতে পারবেন।
                💡 টিপ: ভুল হওয়া প্রশ্নগুলোর ব্যাখ্যা সেকশনটি ভালো করে পড়ুন এবং বারবার প্র্যাক্টিস করুন।
                """.trimIndent()
            }
            else -> {
                """
                📚 মনোবল হারাবেন না! ভুল করাই শেখার প্রথম ধাপ।
                🔍 $lesson টপিকটিতে আপনার আরও একটু রিভিশন প্রয়োজন। 
                💡 টিপ: লেসনটি আবার শুরু থেকে মন দিয়ে রিডিং পড়ুন এবং ভয়েস মোডে উচ্চারণ শুনে শুনে নিজের প্রস্তুতি আরও উন্নত করুন।
                """.trimIndent()
            }
        }
    }
}
