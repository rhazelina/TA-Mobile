package com.example.ritamesa.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    // GANTI dengan IP server Laravel Anda
    // Untuk emulator: "http://10.0.2.2:8000/api/"
    // Untuk device fisik: "http://192.168.x.x:8000/api/"
    private const val BASE_URL = "http://10.0.2.2:8000/api/"
    // localhost:8000
    
    private var retrofit: Retrofit? = null
    
    fun getInstance(context: Context): ApiService {
        if (retrofit == null) {
            val sessionManager = SessionManager(context)
            
            // Logging interceptor untuk debugging
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            // OkHttp client dengan interceptors
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(sessionManager))
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        
        return retrofit!!.create(ApiService::class.java)
    }
    
    // Fungsi untuk reset instance (berguna saat logout)
    fun resetInstance() {
        retrofit = null
    }
}
