package com.construrrenta.app.data.api

import android.content.Context
import com.construrrenta.app.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // URL base para el emulador conectándose al localhost de tu PC
    private const val BASE_URL = "http://10.0.2.2:8080/api/v1/"
    private var retrofit: Retrofit? = null

    fun getInstance(context: Context): Retrofit {
        if (retrofit == null) {
            val sessionManager = SessionManager(context)

            // Permite ver los JSONs en el Logcat de Android Studio
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = AuthInterceptor(sessionManager)

            // Ensamblaje del cliente con los interceptores
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }
}